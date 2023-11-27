// Alunos - Gustavo Canellas Aveline Rocha, Rodrigo Renck, Leonardo Ramos
// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// VM
//    HW = memória, cpu
//    SW = tratamento int e chamada de sistema
// Funcionalidades de carga, execução e dump de memória

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Sistema {

    // -------------------------------------------------------------------------------------------------------
    // --------------------- H A R D W A R E - definicoes de HW ----------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // --------------------- M E M O R I A -  definicoes de palavra de memoria, memória ----------------------

    public class Memory {
        public int tamMem;
        public Word[] m;                  // m representa a memória fisica:   um array de posicoes de memoria (word)

        public Memory(int size){
            tamMem = size;
            m = new Word[tamMem];
            for (int i=0; i<tamMem; i++) { m[i] = new Word(Opcode.___,-1,-1,-1); };
        }

        public void dump(Word w) {        // funcoes de DUMP nao existem em hardware - colocadas aqui para facilidade
            System.out.print("[ ");
            System.out.print(w.opc); System.out.print(", ");
            System.out.print(w.r1);  System.out.print(", ");
            System.out.print(w.r2);  System.out.print(", ");
            System.out.print(w.p);  System.out.println("  ] ");
        }
        public void dump(int ini, int fim) {
            for (int i = ini; i < fim; i++) {
                System.out.print(i); System.out.print(":  ");  dump(m[i]);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------

    public class Word { 	// cada posicao da memoria tem uma instrucao (ou um dado)
        public Opcode opc; 	//
        public int r1; 		// indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
        public int r2; 		// indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
        public int p; 		// parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

        public Word(Opcode _opc, int _r1, int _r2, int _p) {  // vide definição da VM - colunas vermelhas da tabela
            opc = _opc;   r1 = _r1;    r2 = _r2;	p = _p;
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // --------------------- C P U  -  definicoes da CPU -----------------------------------------------------

    public enum Opcode {
        DATA, ___,		                    // se memoria nesta posicao tem um dado, usa DATA, se nao usada ee NULO ___
        JMP, JMPI, JMPIG, JMPIL, JMPIE,     // desvios e parada
        JMPIM, JMPIGM, JMPILM, JMPIEM, STOP,
        JMPIGK, JMPILK, JMPIEK, JMPIGT,
        ADDI, SUBI, ADD, SUB, MULT,         // matematicos
        LDI, LDD, STD, LDX, STX, MOVE,      // movimentacao
        TRAP                                // chamada de sistema
    }

    public enum Interrupts {               // possiveis interrupcoes que esta CPU gera
        noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow, intSTOP, intTimeSliceFinish,intIO;
    }

    public class CPU extends Thread{
        private int maxInt; // valores maximo e minimo para inteiros nesta cpu
        private int minInt;
        // característica do processador: contexto da CPU ...
        public int pc; 			// ... composto de program counter,
        private Word ir; 			// instruction register,
        public int[] reg;       	// registradores da CPU
        private Interrupts irpt; 	// durante instrucao, interrupcao pode ser sinalizada

        private List<Interrupts> irptIO = new ArrayList<>(); // Lista para todas interrupcoes de IO
        private Semaphore semaforoInterrupcaoIO = new Semaphore(1);
        private int base;   		// base e limite de acesso na memoria
        private int limite; // por enquanto toda memoria pode ser acessada pelo processo rodando
        // ATE AQUI: contexto da CPU - tudo que precisa sobre o estado de um processo para executa-lo
        // nas proximas versoes isto pode modificar

        private Memory mem;               // mem tem funcoes de dump e o array m de memória 'fisica'
        private Word[] m;                 // CPU acessa MEMORIA, guarda referencia a 'm'. m nao muda. semre será um array de palavras

        private InterruptHandling ih;     // significa desvio para rotinas de tratamento de  Int - se int ligada, desvia
        private SysCallHandling sysCall;  // significa desvio para tratamento de chamadas de sistema - trap
        private boolean debug;            // se true entao mostra cada instrucao em execucao
        private boolean debugResposta=false;
        private ArrayList<Integer> tabelaDePaginas;
        private int particao;
        private int clock;
        private int robin;

        public PCBparticao processoAtivoParticao;
        public PCBpaginacao processoAtivoPaginacao;

        public CPU(Memory _mem, InterruptHandling _ih, SysCallHandling _sysCall, boolean _debug) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
            maxInt =  32767;        // capacidade de representacao modelada
            minInt = -32767;        // se exceder deve gerar interrupcao de overflow
            mem = _mem;	            // usa mem para acessar funcoes auxiliares (dump)
            m = mem.m; 				// usa o atributo 'm' para acessar a memoria.
            reg = new int[10]; 		// aloca o espaço dos registradores - regs 8 e 9 usados somente para IO
            ih = _ih;               // aponta para rotinas de tratamento de int
            sysCall = _sysCall;     // aponta para rotinas de tratamento de chamadas de sistema
            debug =  _debug;        // se true, print da instrucao em execucao
            clock = 0;
            robin = 5;
        }

        private boolean legalParticao(int e) {                             // todo acesso a memoria tem que ser verificado
            if(e<=limite) return true;
            else return false;
        }

        private boolean legalPaginacao(int e) {                             // todo acesso a memoria tem que ser verificado
            if((e/gp.gmPaginacao.tamFrames)<=limite) return true;
            else return false;
        }

        private boolean testOverflow(int v) {                       // toda operacao matematica deve avaliar se ocorre overflow
            if ((v < minInt) || (v > maxInt)) {
                irpt = Interrupts.intOverflow;
                return false;
            };
            return true;
        }

        public void setContextPaginacao(int _base, int _limite, int _pc, ArrayList<Integer> _tabelaDePaginas, int[] _regs) {  // no futuro esta funcao vai ter que ser
            base = _base;                                          // expandida para setar todo contexto de execucao,
            limite = _limite;									   // agora,  setamos somente os registradores base,
            pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
            irpt = Interrupts.noInterrupt;                         // reset da interrupcao registrada
            tabelaDePaginas = _tabelaDePaginas;
            reg = _regs;
        }

        public void setContextParticao(int _base, int _limite, int _pc,int _particao, int[] _regs) {  // no futuro esta funcao vai ter que ser
            base = _base;                                          // expandida para setar todo contexto de execucao,
            limite = _limite;									   // agora,  setamos somente os registradores base,
            pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
            irpt = Interrupts.noInterrupt;                         // reset da interrupcao registrada
            particao = _particao;
            reg = _regs;
        }

        public void runParticao() {
            // execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
            while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
                if(processoAtivoParticao==null){
                    escalonarParticao();
                }
                // --------------------------------------------------------------------------------------------------
                // FETCH
                int endereco;
                //System.out.println("r0:"+reg[0] + " r1:"+reg[1]+" r2:"+reg[2]+" r3:"+reg[3]+" r4:"+reg[4]+" r5:"+reg[5]+" r6:"+reg[6]+" r7:"+reg[7]+" r8:"+reg[8]+" r9:"+reg[9]);
                if (legalParticao(endereco = gp.gmParticao.traducao(particao,pc))) { 	// pc valido
                    ir = m[endereco]; 	// <<<<<<<<<<<<           busca posicao da memoria apontada por pc, guarda em ir
                    if (debug) {System.out.print("                               pc: "+pc+"       exec: ");  mem.dump(ir); }
                    // --------------------------------------------------------------------------------------------------
                    // EXECUTA INSTRUCAO NO ir
                    switch (ir.opc) {   // conforme o opcode (código de operação) executa

                        // Instrucoes de Busca e Armazenamento em Memoria
                        case LDI: // Rd ← k
                            reg[ir.r1] = ir.p;
                            pc++;
                            break;

                        case LDD: // Rd <- [A]
                            if (legalParticao(ir.p)) {
                                reg[ir.r1] = m[gp.gmParticao.traducao(particao,ir.p)].p;
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case LDX: // RD <- [RS] // NOVA
                            if (legalParticao(reg[ir.r2])) {
                                reg[ir.r1] = m[gp.gmParticao.traducao(particao,reg[ir.r2])].p;
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case STD: // [A] ← Rs
                            if (legalParticao(ir.p)) {
                                m[gp.gmParticao.traducao(particao,ir.p)].opc = Opcode.DATA;
                                m[gp.gmParticao.traducao(particao,ir.p)].p = reg[ir.r1];
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case STX: // [Rd] ←Rs
                            if (legalParticao(reg[ir.r1])) {
                                m[gp.gmParticao.traducao(particao,reg[ir.r1])].opc = Opcode.DATA;
                                m[gp.gmParticao.traducao(particao,reg[ir.r1])].p = reg[ir.r2];
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case MOVE: // RD <- RS
                            reg[ir.r1] = reg[ir.r2];
                            pc++;
                            break;

                        // Instrucoes Aritmeticas
                        case ADD: // Rd ← Rd + Rs
                            reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case ADDI: // Rd ← Rd + k
                            reg[ir.r1] = reg[ir.r1] + ir.p;
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case SUB: // Rd ← Rd - Rs
                            reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case SUBI: // RD <- RD - k // NOVA
                            reg[ir.r1] = reg[ir.r1] - ir.p;
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case MULT: // Rd <- Rd * Rs
                            reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        // Instrucoes JUMP
                        case JMP: // PC <- k
                            pc = ir.p;
                            break;

                        case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (reg[ir.r2] > 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIGK: // If RC > 0 then PC <- k else PC++
                            if (reg[ir.r2] > 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPILK: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.r2] < 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIEK: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.r2] == 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;


                        case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                            if (reg[ir.r2] < 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                            if (reg[ir.r2] == 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIM:// Here
                            pc = m[gp.gmParticao.traducao(particao,ir.p)].p;
                            break;

                        case JMPIGM: // if Rc > 0 then PC <- [A] Else PC <- PC +1 // Here
                            if (reg[ir.r2] > 0) {
                                pc = m[gp.gmParticao.traducao(particao,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPILM: // if Rc < 0 then PC <- [A] Else PC <- PC +1 //Here
                            if (reg[ir.r2] < 0) {
                                pc = m[gp.gmParticao.traducao(particao,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIEM: // if Rc = 0 then PC <- [A] Else PC <- PC +1
                            if (reg[ir.r2] == 0) {
                                pc = m[gp.gmParticao.traducao(particao,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGT: // If RS>RC then PC <- k else PC++
                            if (reg[ir.r1] > reg[ir.r2]) {
                                pc = m[gp.gmParticao.traducao(particao,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        // outras
                        case STOP: // por enquanto, para execucao
                            irpt = Interrupts.intSTOP;
                            break;

                        case DATA:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        // Chamada de sistema
                        case TRAP:
                            sysCall.handle(processoAtivoParticao.getId());            // <<<<< aqui desvia para rotina de chamada de sistema, no momento so temos IO
                            pc++;
                            break;

                        // Inexistente
                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }else irpt = Interrupts.intEnderecoInvalido;

                // --------------------------------------------------------------------------------------------------
                // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
                if (!(irpt == Interrupts.noInterrupt)) {   // existe interrupção
                    if(irpt==Interrupts.intSTOP)ih.handleStop(irpt,pc, processoAtivoParticao.getId());
                    else ih.handle(irpt,pc);                       // desvia para rotina de tratamento
                    break; // break sai do loop da cpu
                }
                verificaInterrupcaoDeIO(pc);

                clock++;
                if(clock==robin){
                    irpt = Interrupts.intTimeSliceFinish;
                    ih.handleTimer(irpt,pc, processoAtivoParticao.getId());
                }

            }  // FIM DO CICLO DE UMA INSTRUÇÃO
        }

        public void runPaginacao() {
            // execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado
            while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
                if(processoAtivoPaginacao==null){
                    escalonarPaginacao();
                }
                // --------------------------------------------------------------------------------------------------
                // FETCH
                int endereco;
                //System.out.println("r0:"+reg[0] + " r1:"+reg[1]+" r2:"+reg[2]+" r3:"+reg[3]+" r4:"+reg[4]+" r5:"+reg[5]+" r6:"+reg[6]+" r7:"+reg[7]+" r8:"+reg[8]+" r9:"+reg[9]);
                if (legalPaginacao(endereco = gp.gmPaginacao.traducao(tabelaDePaginas,pc))) { 	// pc valido
                    ir = m[endereco]; 	// <<<<<<<<<<<<           busca posicao da memoria apontada por pc, guarda em ir
                    if (debug) {System.out.print("                               pc: "+pc+"       exec: ");  mem.dump(ir); }
                    // --------------------------------------------------------------------------------------------------
                    // EXECUTA INSTRUCAO NO ir
                    switch (ir.opc) {   // conforme o opcode (código de operação) executa

                        // Instrucoes de Busca e Armazenamento em Memoria
                        case LDI: // Rd ← k
                            reg[ir.r1] = ir.p;
                            pc++;
                            break;

                        case LDD: // Rd <- [A]
                            if (legalPaginacao(ir.p)) {
                                reg[ir.r1] = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case LDX: // RD <- [RS] // NOVA
                            if (legalPaginacao(reg[ir.r2])) {
                                reg[ir.r1] = m[gp.gmPaginacao.traducao(tabelaDePaginas,reg[ir.r2])].p;
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case STD: // [A] ← Rs
                            if (legalPaginacao(ir.p)) {
                                m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].opc = Opcode.DATA;
                                m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p = reg[ir.r1];
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case STX: // [Rd] ←Rs
                            if (legalPaginacao(reg[ir.r1])) {
                                m[gp.gmPaginacao.traducao(tabelaDePaginas,reg[ir.r1])].opc = Opcode.DATA;
                                m[gp.gmPaginacao.traducao(tabelaDePaginas,reg[ir.r1])].p = reg[ir.r2];
                                pc++;
                            } else irpt = Interrupts.intEnderecoInvalido;
                            break;

                        case MOVE: // RD <- RS
                            reg[ir.r1] = reg[ir.r2];
                            pc++;
                            break;

                        // Instrucoes Aritmeticas
                        case ADD: // Rd ← Rd + Rs
                            reg[ir.r1] = reg[ir.r1] + reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case ADDI: // Rd ← Rd + k
                            reg[ir.r1] = reg[ir.r1] + ir.p;
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case SUB: // Rd ← Rd - Rs
                            reg[ir.r1] = reg[ir.r1] - reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case SUBI: // RD <- RD - k // NOVA
                            reg[ir.r1] = reg[ir.r1] - ir.p;
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        case MULT: // Rd <- Rd * Rs
                            reg[ir.r1] = reg[ir.r1] * reg[ir.r2];
                            testOverflow(reg[ir.r1]);
                            pc++;
                            break;

                        // Instrucoes JUMP
                        case JMP: // PC <- k
                            pc = ir.p;
                            break;

                        case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (reg[ir.r2] > 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIGK: // If RC > 0 then PC <- k else PC++
                            if (reg[ir.r2] > 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPILK: // If RC < 0 then PC <- k else PC++
                            if (reg[ir.r2] < 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIEK: // If RC = 0 then PC <- k else PC++
                            if (reg[ir.r2] == 0) {
                                pc = ir.p;
                            } else {
                                pc++;
                            }
                            break;


                        case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                            if (reg[ir.r2] < 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                            if (reg[ir.r2] == 0) {
                                pc = reg[ir.r1];
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIM:// Here
                            pc = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                            break;

                        case JMPIGM: // if Rc > 0 then PC <- [A] Else PC <- PC +1 // Here
                            if (reg[ir.r2] > 0) {
                                pc = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPILM: // if Rc < 0 then PC <- [A] Else PC <- PC +1 //Here
                            if (reg[ir.r2] < 0) {
                                pc = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        case JMPIEM: // if Rc = 0 then PC <- [A] Else PC <- PC +1
                            if (reg[ir.r2] == 0) {
                                pc = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGT: // If RS>RC then PC <- k else PC++
                            if (reg[ir.r1] > reg[ir.r2]) {
                                pc = m[gp.gmPaginacao.traducao(tabelaDePaginas,ir.p)].p;
                            } else {
                                pc++;
                            }
                            break;

                        // outras
                        case STOP: // por enquanto, para execucao
                            irpt = Interrupts.intSTOP;
                            break;

                        case DATA:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;

                        // Chamada de sistema
                        case TRAP:
                            sysCall.handle(processoAtivoPaginacao.getId());            // <<<<< aqui desvia para rotina de chamada de sistema, no momento so temos IO
                            pc++;
                            break;

                        // Inexistente
                        default:
                            irpt = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }else irpt = Interrupts.intEnderecoInvalido;

                // --------------------------------------------------------------------------------------------------
                // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
                if (!(irpt == Interrupts.noInterrupt)) {   // existe interrupção
                    if(irpt==Interrupts.intSTOP)ih.handleStop(irpt,pc, processoAtivoPaginacao.getId());
                    else ih.handle(irpt,pc);                       // desvia para rotina de tratamento
                    break; // break sai do loop da cpu
                }
                verificaInterrupcaoDeIO(pc);

                clock++;

                if(clock==robin){
                    irpt = Interrupts.intTimeSliceFinish;
                    ih.handleTimer(irpt,pc,processoAtivoPaginacao.id);
                }
            }  // FIM DO CICLO DE UMA INSTRUÇÃO
        }

        public void escalonarParticao(){
            vm.cpu.clock=0;
            if(processoAtivoParticao==null ) {
                while(true){
                    verificaInterrupcaoDeIO(0);

                    gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

                    if(!gp.listaDeProcessosProntosParticao.isEmpty()){

                        processoAtivoParticao = gp.listaDeProcessosProntosParticao.get(0);

                        processoAtivoParticao.setEstado("Rodando");
                        vm.cpu.setContextParticao(0,
                                gp.gmParticao.traducao(gp.gmParticao.tamPart,0),
                                processoAtivoParticao.contexto.pc,
                                processoAtivoParticao.particao,
                                processoAtivoParticao.contexto.regs);


                        gp.listaDeProcessosProntosParticao.remove(0);

                        gp.semaforoDeAcessoListaProntos.release();

                        break;
                    }
                    gp.semaforoDeAcessoListaProntos.release();
                }
            } else{
                gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

                processoAtivoParticao.setEstado("Pronto");

                gp.listaDeProcessosProntosParticao.add(processoAtivoParticao); //adiciona o processo ativo no final da fila

                processoAtivoParticao = gp.listaDeProcessosProntosParticao.get(0); // pega primeiro da lista e coloca como novo ativo

                processoAtivoParticao.setEstado("Rodando");

                vm.cpu.setContextParticao(0,
                        gp.gmParticao.traducao(gp.gmParticao.tamPart,0),
                        processoAtivoParticao.contexto.pc,
                        processoAtivoParticao.particao,
                        processoAtivoParticao.contexto.regs); //seta o contexto desse processo

                gp.listaDeProcessosProntosParticao.remove(0); // tira da lista de prontos

                clock=0;
                gp.semaforoDeAcessoListaProntos.release();
            }
        }

        public void escalonarPaginacao(){
            vm.cpu.clock=0;
            if(processoAtivoPaginacao==null){
                while(true){
                    verificaInterrupcaoDeIO(0);

                    gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

                    if(!gp.listaDeProcessosProntosPaginacao.isEmpty()){
                        processoAtivoPaginacao = gp.listaDeProcessosProntosPaginacao.get(0);

                        processoAtivoPaginacao.setEstado("Rodando");

                        vm.cpu.setContextPaginacao(0,
                                gp.gmPaginacao.traducao(processoAtivoPaginacao.tabelaDePaginas,gp.gmPaginacao.tamPag),
                                processoAtivoPaginacao.contexto.pc,
                                processoAtivoPaginacao.tabelaDePaginas,
                                processoAtivoPaginacao.contexto.regs);

                        gp.listaDeProcessosProntosPaginacao.remove(0);

                        gp.semaforoDeAcessoListaProntos.release();

                        break;
                    }
                    gp.semaforoDeAcessoListaProntos.release();
                }
            }else{
                gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

                processoAtivoPaginacao.setEstado("Pronto");

                gp.listaDeProcessosProntosPaginacao.add(processoAtivoPaginacao);

                processoAtivoPaginacao = gp.listaDeProcessosProntosPaginacao.get(0);

                processoAtivoPaginacao.setEstado("Rodando");

                vm.cpu.setContextPaginacao(0,
                        gp.gmPaginacao.traducao(processoAtivoPaginacao.tabelaDePaginas,gp.gmPaginacao.tamPag),
                        processoAtivoPaginacao.contexto.pc,
                        processoAtivoPaginacao.tabelaDePaginas,
                        processoAtivoPaginacao.contexto.regs);

                gp.listaDeProcessosProntosPaginacao.remove(0);

                clock=0;
                gp.semaforoDeAcessoListaProntos.release();
            }
        }

        public void verificaInterrupcaoDeIO(int pc){
            semaforoInterrupcaoIO.acquireUninterruptibly();
            if(!irptIO.isEmpty()){
                ih.handleIO(irptIO.remove(0),pc);
            }
            semaforoInterrupcaoIO.release();
        }

        @Override
        public void run() {
            if(gp.gmParticao!=null) {
                while (true){
                    vm.cpu.runParticao();
                }
            }
            else {
                while (true){
                    vm.cpu.runPaginacao();
                }
            }
        }
    }
    // ------------------ C P U - fim ------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------



    // ------------------- V M  - constituida de CPU e MEMORIA -----------------------------------------------
    // -------------------------- atributos e construcao da VM -----------------------------------------------
    public class VM {
        public int tamMem;
        public Word[] m;
        public Memory mem;
        public CPU cpu;

        public VM(InterruptHandling ih, SysCallHandling sysCall){
            // vm deve ser configurada com endereço de tratamento de interrupcoes e de chamadas de sistema
            // cria memória
            tamMem = 1024;
            mem = new Memory(tamMem);
            m = mem.m;
            // cria cpu
            cpu = new CPU(mem,ih,sysCall, false);                   // true liga debug
        }
    }
    // ------------------- V M  - fim ------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // --------------------H A R D W A R E - fim -------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // ------------------- S O F T W A R E - inicio ----------------------------------------------------------

    // ------------------- I N T E R R U P C O E S  - rotinas de tratamento ----------------------------------
    public class InterruptHandling {
        public void handle(Interrupts irpt, int pc) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
            System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);

            //Kill
            gp.desalocaProcesso(vm.cpu.processoAtivoParticao);

            vm.cpu.processoAtivoParticao=null;
        }

        public void handleTimer(Interrupts irpt, int pc, int processoId) {
            System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc + "   id: "+processoId);

            Contexto contexto = new Contexto(vm.cpu.reg,vm.cpu.pc);

            if(vm.cpu.processoAtivoParticao!=null){
                vm.cpu.processoAtivoParticao.setContexto(contexto);
                vm.cpu.processoAtivoParticao.setEstado("Finalizado");
                vm.cpu.escalonarParticao();
            }else{
                vm.cpu.processoAtivoPaginacao.setContexto(contexto);
                vm.cpu.processoAtivoPaginacao.setEstado("Finalizado");
                vm.cpu.escalonarPaginacao();
            }
        }

        public void handleStop(Interrupts irpt, int pc,int processoId) {
            System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc + "   id: "+processoId);

            if(vm.cpu.processoAtivoParticao!=null){
                vm.cpu.processoAtivoParticao.setEstado("finalizado");

                if(vm.cpu.debugResposta)dumpProcessoAtivoParticao(vm.cpu.processoAtivoParticao);

                gp.desalocaProcesso(vm.cpu.processoAtivoParticao);

                vm.cpu.processoAtivoParticao=null;
            }else{
                vm.cpu.processoAtivoPaginacao.setEstado("finalizado");

                if(vm.cpu.debugResposta)dumpProcessoAtivoPaginacao(vm.cpu.processoAtivoPaginacao);

                gp.desalocaProcesso(vm.cpu.processoAtivoPaginacao);

                vm.cpu.processoAtivoPaginacao=null;
            }
        }

        public void handleIO(Interrupts irpt, int pc) {
            System.out.println("                                               Interrupcao "+ irpt+ "   pc: "+pc);

            if(gp.gmParticao!=null){
                console.semaforoDaFilaDePedidos.acquireUninterruptibly();
                var processoPedido = console.filaPedidosParticao.remove(0);
                console.semaforoDaFilaDePedidos.release();

                processoPedido.contexto.pc = processoPedido.contexto.pc+1;

                gp.liberaProcessoBloqueadoParticao(processoPedido);
            }else{
                console.semaforoDaFilaDePedidos.acquireUninterruptibly();
                var processoPedido = console.filaPedidosPaginacao.remove(0);
                console.semaforoDaFilaDePedidos.release();

                processoPedido.contexto.pc = processoPedido.contexto.pc+1;

                gp.liberaProcessoBloqueadoPaginacao(processoPedido);
            }
        }
    }

    // ------------------- C H A M A D A S  D E  S I S T E M A  - rotinas de tratamento ----------------------
    public class SysCallHandling {
        private VM vm;
        public void setVM(VM _vm){
            vm = _vm;
        }
        public void handle(int processoId) {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
            System.out.println("                                               Chamada de Sistema com op  /  par:  "+ vm.cpu.reg[8] + " / " + vm.cpu.reg[9] + "  ID: "+processoId);

            if(vm.cpu.reg[8]==1 || vm.cpu.reg[8]==2){
                Contexto contexto = new Contexto(vm.cpu.reg,vm.cpu.pc);

                if(vm.cpu.processoAtivoParticao!=null){
                    PCBparticao processoAtivo = vm.cpu.processoAtivoParticao;

                    processoAtivo.setContexto(contexto);
                    processoAtivo.setEstado("Bloqueado");

                    gp.bloqueiaProcessoParticao(processoAtivo);

                    console.semaforoDaFilaDePedidos.acquireUninterruptibly();
                    console.filaPedidosParticao.add(processoAtivo);
                    console.semaforoDaFilaDePedidos.release();

                    vm.cpu.processoAtivoParticao = null;
                }else{
                    PCBpaginacao processoAtivo = vm.cpu.processoAtivoPaginacao;

                    processoAtivo.setContexto(contexto);
                    processoAtivo.setEstado("Bloqueado");

                    gp.semaforoDeAcessoListaBloqueados.acquireUninterruptibly();
                    gp.listaDeProcessosBloqueadosPaginacao.add(processoAtivo);
                    gp.semaforoDeAcessoListaBloqueados.release();

                    console.semaforoDaFilaDePedidos.acquireUninterruptibly();
                    console.filaPedidosPaginacao.add(processoAtivo);
                    console.semaforoDaFilaDePedidos.release();

                    vm.cpu.processoAtivoPaginacao = null;
                }
            }else{
                //Erro, pela definicao da maquina virtual

                //Kill
                gp.desalocaProcesso(vm.cpu.processoAtivoParticao);

                vm.cpu.processoAtivoParticao=null;
            }
        }
    }

    // ------------------ U T I L I T A R I O S   D O   S I S T E M A -----------------------------------------
    // ------------------ load é invocado a partir de requisição do usuário

    //descontinuada, versao t1
    //funcao de execucao
    private boolean executa(int id){
        if(this.gp.gmParticao!=null){
            PCBparticao processo = this.gp.getProcessoPeloIdParticao(id);

            if(processo==null)return false;

            PCBparticao processoRodando = this.gp.setProcessoRodandoParticao();

            if(processoRodando!=null) processoRodando.setEstado("Pronto");

            processo.setEstado("Rodando");
//			vm.cpu.setContextParticao(0,this.gp.gmParticao.tamPart, 0,processo.particao);
            vm.cpu.runParticao();
        }else{
            PCBpaginacao processo = this.gp.getProcessoPeloIdPaginacao(id);

            if (processo==null)return false;

            PCBpaginacao processoRodando = this.gp.setProcessoRodandoPaginacao();

            if(processoRodando!=null) processoRodando.setEstado("Pronto");

            processo.setEstado("Rodando");
//            vm.cpu.setContextPaginacao(0, this.gp.gmPaginacao.tamPag, 0,processo.tabelaDePaginas);
            vm.cpu.runPaginacao();
        }
        return true;
    }

    private void dumpProcesso(int id){
        if(this.gp.gmParticao!=null){
            var processo = this.gp.getProcessoPeloIdParticao(id);

            if(processo==null) System.out.println("Processo nao encontrado, verifique a lista de processos ativos!");
            else{
                System.out.println();
                System.out.println("Id do processo: " + processo.id);
                System.out.println("Particao do processo: " + processo.particao);
                System.out.println("Estado do processo:" + processo.estado);
                //destaque
                System.out.println("Contexto do processo:" + processo.contexto.pc);
                System.out.println("Conteudo do processo:");
                int enderecoFisicoInicial = this.gp.gmParticao.traducao(processo.particao,0);
                int enderecoFisicoFinal = enderecoFisicoInicial + this.gp.gmParticao.tamPart;
                this.vm.mem.dump(enderecoFisicoInicial,enderecoFisicoFinal);
            }
        }else{
            var processo = this.gp.getProcessoPeloIdPaginacao(id);

            if(processo==null) System.out.println("Processo nao encontrado, verifique a lista de processos ativos!");
            else {
                ArrayList<Integer> listaPc = new ArrayList<Integer>();
                ArrayList<Integer>  listaLimit = new ArrayList<Integer>();

                System.out.println();
                System.out.println("Id do processo = " + processo.id);
                System.out.println("Estado do processo = " + processo.estado);
                for (int pagina:processo.tabelaDePaginas) {
                    System.out.println("Pagina do processo = " + pagina);

                    int enderecoLogicoInicial = pagina*this.gp.gmPaginacao.tamFrames;
                    int enderecoLogicoFinal = enderecoLogicoInicial+this.gp.gmPaginacao.tamFrames-1;
                    listaPc.add(enderecoLogicoInicial);
                    listaLimit.add(enderecoLogicoFinal);
                }
                System.out.println("Conteudo do processo:");
                for (int i=0;i<listaPc.size();i++){
                    this.vm.mem.dump(listaPc.get(i),listaLimit.get(i)+1);
                }
            }
        }
    }

    private void dumpProcesso(PCBparticao processo) {
        if (this.gp.gmParticao != null) {
            if (processo == null) System.out.println("Processo nao encontrado, verifique a lista de processos ativos!");
            else {
                System.out.println();
                System.out.println("Id do processo: " + processo.id);
                System.out.println("Particao do processo: " + processo.particao);
                System.out.println("Estado do processo:" + processo.estado);
                System.out.println("Conteudo do processo:");
                int enderecoFisicoInicial = this.gp.gmParticao.traducao(processo.particao, 0);
                int enderecoFisicoFinal = enderecoFisicoInicial + this.gp.gmParticao.tamPart;
                this.vm.mem.dump(enderecoFisicoInicial, enderecoFisicoFinal);
            }
        }
    }

    private void dumpProcessoAtivoParticao(PCBparticao processo){
        System.out.println();
        System.out.println("Id do processo: " + processo.id);
        System.out.println("Particao do processo: " + processo.particao);
        System.out.println("Estado do processo:" + processo.estado);
        System.out.println("Conteudo do processo:");
        int enderecoFisicoInicial = this.gp.gmParticao.traducao(processo.particao,0);
        int enderecoFisicoFinal = enderecoFisicoInicial + this.gp.gmParticao.tamPart;
        this.vm.mem.dump(enderecoFisicoInicial,enderecoFisicoFinal);
    }

    private void dumpProcessoAtivoPaginacao(PCBpaginacao processo){
        ArrayList<Integer> listaPc = new ArrayList<Integer>();
        ArrayList<Integer>  listaLimit = new ArrayList<Integer>();

        System.out.println();
        System.out.println("Id do processo = " + processo.id);
        System.out.println("Estado do processo = " + processo.estado);
        for (int pagina:processo.tabelaDePaginas) {
            System.out.println("Pagina do processo = " + pagina);

            int enderecoLogicoInicial = pagina*this.gp.gmPaginacao.tamFrames;
            int enderecoLogicoFinal = enderecoLogicoInicial+this.gp.gmPaginacao.tamFrames-1;
            listaPc.add(enderecoLogicoInicial);
            listaLimit.add(enderecoLogicoFinal);
        }
        System.out.println("Conteudo do processo:");
        for (int i=0;i<listaPc.size();i++){
            this.vm.mem.dump(listaPc.get(i),listaLimit.get(i)+1);
        }
    }

    private void listaProcessos(){
        if(this.gp.gmParticao!=null){
            gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

            ArrayList<PCBparticao> listaProcessos = this.gp.listaDeProcessosProntosParticao;
            if(listaProcessos.isEmpty()) System.out.println("Lista de processos vazia.");
            else{
                for (PCBparticao processo:listaProcessos) {
                    System.out.println();
                    System.out.println("Id do processo = " + processo.id);
                    System.out.println("Particao do processo = " + processo.particao);
                    System.out.println("Estado do processo = " + processo.estado);
                }
            }

            gp.semaforoDeAcessoListaProntos.release();

            gp.semaforoDeAcessoListaBloqueados.acquireUninterruptibly();

            ArrayList<PCBparticao> listaProcessosBloquados = this.gp.listaDeProcessosBloqueadosParticao;
            if(listaProcessosBloquados.isEmpty()) System.out.println("Lista de processos vazia.");
            else{
                for (PCBparticao processo:listaProcessosBloquados) {
                    System.out.println();
                    System.out.println("Id do processo = " + processo.id);
                    System.out.println("Particao do processo = " + processo.particao);
                    System.out.println("Estado do processo = " + processo.estado);
                }
            }

            gp.semaforoDeAcessoListaBloqueados.release();
        }else{
            gp.semaforoDeAcessoListaProntos.acquireUninterruptibly();

            ArrayList<PCBpaginacao> listaProcessos = this.gp.listaDeProcessosProntosPaginacao;
            if(listaProcessos.isEmpty()) System.out.println("Lista de processos vazia");
            else {
                for (PCBpaginacao processo:listaProcessos) {
                    System.out.println();
                    System.out.println("Id do processo = " + processo.id);
                    System.out.println("Estado do processo = " + processo.estado);
                    for (int pagina:processo.tabelaDePaginas) {
                        System.out.println("Pagina do processo = " + pagina);
                    }
                }
            }

            gp.semaforoDeAcessoListaProntos.release();

            gp.semaforoDeAcessoListaBloqueados.acquireUninterruptibly();

            listaProcessos = this.gp.listaDeProcessosBloqueadosPaginacao;
            if(listaProcessos.isEmpty()) System.out.println("Lista de processos vazia");
            else {
                for (PCBpaginacao processo:listaProcessos) {
                    System.out.println();
                    System.out.println("Id do processo = " + processo.id);
                    System.out.println("Estado do processo = " + processo.estado);
                    for (int pagina:processo.tabelaDePaginas) {
                        System.out.println("Pagina do processo = " + pagina);
                    }
                }
            }

            gp.semaforoDeAcessoListaBloqueados.release();
        }
    }

    public class GMParticao{
        public int tamPart;
        public int numParticoes;
        public boolean[] alocado;

        public Memory memory;

        public GMParticao(int tamPart, Memory mem){
            this.tamPart = tamPart;
            this.memory = mem;
            this.numParticoes = mem.tamMem/tamPart;
            alocado = new boolean[numParticoes];
        }

        public int aloca(int programa){

            int tamProg = programa;
            if(tamProg>tamPart) return-1;

            for (int i = 0; i<alocado.length-1; i++){
                if(!alocado[i]) {
                    alocado[i] = true;
                    return i;
                }
            }

            return -1;
        }

        public void desaloca(int part){
            alocado[part] = false;
        }

        public int carga(int enderecoLogico, Word[] programa){

            int enderecoFisico = traducao(enderecoLogico,0);
            var enderecoFisicoFinal = enderecoFisico + tamPart;
            var posicPrograma = 0;
            for (int i = enderecoFisico; i<enderecoFisicoFinal-1; i++){
                if(posicPrograma<programa.length-1){
                    memory.m[i] = programa[posicPrograma];
                }else{
                    memory.m[i] = new Word(Opcode.___, -1,-1,-1);
                }

                posicPrograma++;
            }
            return enderecoLogico;
        }

        public int traducao(int enderecoLogico,int offset){
//			System.out.println(enderecoLogico*tamPart+offset);
            return enderecoLogico*tamPart+offset;
        }
    }

    public class GMPaginacao{
        public int tamPag;
        public int tamMem;
        public int tamFrames;
        public boolean[] framesAlocados;
        public Memory memory;


        public GMPaginacao(int tamMem, int tamPag,Memory memory) {
            this.tamPag = tamPag;
            this.tamFrames = tamPag;
            this.tamMem = tamMem;
            this.memory=memory;

            int frames = tamMem / tamPag;
            this.framesAlocados = new boolean[frames];
        }

        public boolean aloca(int nroPalavras, ArrayList<Integer> tabelaPaginas){
            int nroDePaginasNecessarias = nroPalavras/tamPag;

            int framesDisponiveis=0;
            for (boolean frame:framesAlocados) {
                //verifica se o frame esta disponivel
                if(!frame) framesDisponiveis++;
            }

            //se o numero de paginas necessarias for maior que os frames disponiveis nao eh possivel alocar
            if(nroDePaginasNecessarias<framesDisponiveis) {
                for(int i=0;i<=nroDePaginasNecessarias;i++){
                    for (int j=0;j<framesAlocados.length;j++) {
                        if (!framesAlocados[j]){
                            framesAlocados[j]=true;
                            tabelaPaginas.add(j);
                            break;
                        }
                    }
                }
                return true;
            }
            else{
                return false;
            }
        }

        public void desalocaPaginas(ArrayList<Integer> tabelaDePaginas) {
            for (int p : tabelaDePaginas) {
                framesAlocados[p]=false;
            }
        }

        public int traducao(ArrayList<Integer> tabelaDePaginas,int enderecoLogico){
            int pagina = enderecoLogico/tamFrames;
            int offset = enderecoLogico%tamFrames;

            int paginaDaTabela = tabelaDePaginas.get(pagina);

            int enderecoFisico = paginaDaTabela*tamFrames+offset;
//			System.out.println(paginaDaTabela +" "+offset);
//			System.out.println(enderecoLogico+ " " +enderecoFisico);
            return enderecoFisico;
        }

        public ArrayList<Integer> carga(Word[] programa,ArrayList<Integer> tabelaDePaginas){
            int posicaoPrograma=0;

            for (int pagina: tabelaDePaginas) {
                int enderecoInicial = pagina*tamFrames;
                int enderecoFinal = enderecoInicial+tamFrames-1;

                for(int i=enderecoInicial;i<=enderecoFinal;i++){
                    if(posicaoPrograma<programa.length){
                        memory.m[i] = programa[posicaoPrograma];
                        posicaoPrograma++;
                    }else break;
                }
            }

            return tabelaDePaginas;
        }
    }

    public class GP{
        public ArrayList<PCBparticao> listaDeProcessosProntosParticao = new ArrayList<>();

        public ArrayList<PCBpaginacao> listaDeProcessosProntosPaginacao = new ArrayList<>();
        public ArrayList<PCBparticao> listaDeProcessosBloqueadosParticao = new ArrayList<>();

        public ArrayList<PCBpaginacao> listaDeProcessosBloqueadosPaginacao = new ArrayList<>();
        public GMParticao gmParticao=null;
        public GMPaginacao gmPaginacao=null;
        private int idProcessos=0;

        public Semaphore semaforoDeAcessoListaProntos = new Semaphore(1);
        public Semaphore semaforoDeAcessoListaBloqueados = new Semaphore(1);

        public GP(GMParticao GM) {
            this.gmParticao = GM;
        }

        public GP(GMPaginacao GM) {
            this.gmPaginacao = GM;
        }

        public void liberaProcessoBloqueadoParticao(PCBparticao processoLiberado){
            semaforoDeAcessoListaBloqueados.acquireUninterruptibly();
            listaDeProcessosBloqueadosParticao.remove(processoLiberado);
            semaforoDeAcessoListaBloqueados.release();

            semaforoDeAcessoListaProntos.acquireUninterruptibly();
            listaDeProcessosProntosParticao.add(processoLiberado);
            semaforoDeAcessoListaProntos.release();
        }

        public void bloqueiaProcessoParticao(PCBparticao processoLiberado){
            semaforoDeAcessoListaBloqueados.acquireUninterruptibly();
            listaDeProcessosBloqueadosParticao.add(processoLiberado);
            semaforoDeAcessoListaBloqueados.release();
        }

        public void liberaProcessoBloqueadoPaginacao(PCBpaginacao processoLiberado){
            semaforoDeAcessoListaBloqueados.acquireUninterruptibly();
            listaDeProcessosBloqueadosPaginacao.remove(processoLiberado);
            semaforoDeAcessoListaBloqueados.release();

            semaforoDeAcessoListaProntos.acquireUninterruptibly();
            listaDeProcessosProntosPaginacao.add(processoLiberado);
            semaforoDeAcessoListaProntos.release();
        }

        public PCBparticao getProcessoPeloIdParticao(int id){
            for (PCBparticao processo:listaDeProcessosProntosParticao) {
                if(processo.getId()==id) return processo;
            }
            return null;
        }

        public PCBparticao setProcessoRodandoParticao(){
            for (PCBparticao processo:listaDeProcessosProntosParticao) {
                if(Objects.equals(processo.getEstado(), "Rodando")) return processo;
            }
            return null;
        }

        public PCBpaginacao getProcessoPeloIdPaginacao(int id){
            for (PCBpaginacao processo:listaDeProcessosProntosPaginacao) {
                if(processo.getId()==id) {
                    return processo;
                }
            }
            return null;
        }

        public PCBpaginacao setProcessoRodandoPaginacao(){
            for (PCBpaginacao processo:listaDeProcessosProntosPaginacao) {
                if(Objects.equals(processo.getEstado(), "Rodando")) return processo;
            }
            return null;
        }

        public boolean criaProcesso(Word[] programa){

            if(gmParticao!=null){
                semaforoDeAcessoListaProntos.acquireUninterruptibly();

                int enderecoLogico = gmParticao.aloca(programa.length);

                //programa maior que a particao
                if(enderecoLogico == -1){
                    semaforoDeAcessoListaProntos.release();
                    return false;
                }
                else{

                    int id = this.idProcessos++;
                    int particao = gmParticao.carga(enderecoLogico,programa);
                    int enderecoFisico = gmParticao.traducao(enderecoLogico,0);
                    Contexto contexto = new Contexto(new int[10],0);
                    PCBparticao pcb = new PCBparticao(id,particao,contexto);
                    listaDeProcessosProntosParticao.add(pcb);

                    semaforoDeAcessoListaProntos.release();

                    return true;
                }
            }else {
                semaforoDeAcessoListaProntos.acquireUninterruptibly();

                ArrayList<Integer> tabelaDePaginas = new ArrayList<>();
                boolean temEspaco = gmPaginacao.aloca(programa.length, tabelaDePaginas);

                if(!temEspaco) {
                    semaforoDeAcessoListaProntos.release();
                    return false;
                }
                else{
                    int id = this.idProcessos++;
                    gmPaginacao.carga(programa,tabelaDePaginas);
                    Contexto contexto = new Contexto(new int[10],0);
                    PCBpaginacao pcb = new PCBpaginacao(id,tabelaDePaginas,contexto);
                    listaDeProcessosProntosPaginacao.add(pcb);

                    semaforoDeAcessoListaProntos.release();
                    return true;
                }
            }
        }

        public void desalocaProcesso(PCBparticao processo){
            int enderecoLogico = processo.getParticao();
            gmParticao.desaloca(enderecoLogico);

            int enderecoFisicoInicio = gmParticao.traducao(processo.particao,0);
            int enderecoFisicoFinal = enderecoFisicoInicio+gmParticao.tamPart;

            for (int i = enderecoFisicoInicio;i< enderecoFisicoFinal;i++) {
                gmParticao.memory.m[i] = new Word(Opcode.___, -1,-1,-1);
            }
        }

        public void desalocaProcesso(PCBpaginacao processo) {
            for (int pagina:processo.tabelaDePaginas) {
                int enderecoInicial = pagina* gmPaginacao.tamFrames;
                int enderecoFinal = enderecoInicial+ gmPaginacao.tamFrames-1;

                for (int i = enderecoInicial;i<=enderecoFinal;i++){
                    gmPaginacao.memory.m[i] = new Word(Opcode.___, -1,-1,-1);
                }
            }
            gmPaginacao.desalocaPaginas(processo.tabelaDePaginas);
        }
    }

    public class PCBparticao {
        private int id;
        private String estado;
        private int particao;
        private Contexto contexto;

        public PCBparticao(int id, int particao,Contexto contexto) {
            this.id = id;
            this.estado = "Pronto";
            this.particao = particao;
            this.contexto = contexto;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public int getParticao() {
            return particao;
        }

        public void setParticao(int particao) {
            this.particao = particao;
        }

        public Contexto getContexto() {
            return contexto;
        }

        public void setContexto(Contexto contexto) {
            this.contexto = contexto;
        }
    }

    public class PCBpaginacao {
        private int id;
        private String estado;
        private ArrayList<Integer> tabelaDePaginas;
        private Contexto contexto;

        public PCBpaginacao(int id, ArrayList<Integer> tabelaDePaginas,Contexto contexto) {
            this.id = id;
            this.estado = "Pronto";
            this.tabelaDePaginas = tabelaDePaginas;
            this.contexto = contexto;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public ArrayList<Integer> getTabelaDePaginas() {
            return tabelaDePaginas;
        }

        public void setTabelaDePaginas(ArrayList<Integer> tabelaDePaginas) {
            this.tabelaDePaginas = tabelaDePaginas;
        }

        public Contexto getContexto() {
            return contexto;
        }

        public void setContexto(Contexto contexto) {
            this.contexto = contexto;
        }
    }

    public class Contexto{
        private int[] regs;
        private int pc;

        public Contexto(int[] regs, int pc) {
            this.regs = regs;
            this.pc = pc;
        }

        public int[] getRegs() {
            return regs;
        }

        public void setRegs(int[] regs) {
            this.regs = regs;
        }

        public int getPc() {
            return pc;
        }

        public void setPc(int pc) {
            this.pc = pc;
        }
    }
    // -------------------------------------------------------------------------------------------------------
    // -------------------  S I S T E M A --------------------------------------------------------------------

    public VM vm;
    public InterruptHandling ih;
    public SysCallHandling sysCall;
    public static Programas progs;
    public GP gp;

    public Console console;

    InterfaceUsuario interfaceUsuario;

    public Sistema()  {   // a VM com tratamento de interrupções
        ih = new InterruptHandling();
        sysCall = new SysCallHandling();
        vm = new VM(ih, sysCall);
        sysCall.setVM(vm);
        progs = new Programas();

        // cria gerente memoria
        // escolha de qual gerente de memoria pode ser utilizado
        GMParticao gm = new GMParticao(64,vm.mem);
        //GMPaginacao gm = new GMPaginacao(vm.mem.tamMem,8,vm.mem);

        // cria gerente de processos
        gp = new GP(gm);

        // cria console para respostas de pedidos do sistema
        console = new Console(gm);

        // cria interface com o usuario
        interfaceUsuario = new InterfaceUsuario(this);

        // inicia as threads
        vm.cpu.start();
        interfaceUsuario.start();
        console.start();
    }

    public class Console extends Thread{
        private ArrayList<PCBparticao> filaPedidosParticao;
        private ArrayList<PCBpaginacao> filaPedidosPaginacao;

        public Console(GMParticao Gm) {
            this.filaPedidosParticao = new ArrayList<>();
        }

        public Console(GMPaginacao Gm) {
            this.filaPedidosPaginacao = new ArrayList<>();
        }

        public Semaphore semaforoDaFilaDePedidos = new Semaphore(1);
        public Semaphore semaforoRespostaPedido = new Semaphore(0);
        public Semaphore semaforoLiberaInterface = new Semaphore(0);

        public void lidaPedido(){
            while(true){
                semaforoRespostaPedido.acquireUninterruptibly();
                TrataPedido();
                semaforoLiberaInterface.release();
            }
        }

        private void TrataPedido() {
            if(filaPedidosParticao !=null){
                semaforoDaFilaDePedidos.acquireUninterruptibly();

                if(!filaPedidosParticao.isEmpty()){
                    var processoPedido = filaPedidosParticao.get(0);

                    if (processoPedido.contexto.regs[8] == 1) { //IN
                        System.out.println("Instrucao IN do processo "+ processoPedido.getId()+ " : ");
                        vm.mem.dump(vm.m[gp.gmParticao.traducao(processoPedido.particao,processoPedido.contexto.pc)]);

                        Scanner scanner = new Scanner(System.in);
                        System.out.print("Digite o numero que deseja: ");

                        vm.m[gp.gmParticao.traducao(processoPedido.particao, processoPedido.contexto.regs[9])].p = scanner.nextInt();
                        vm.m[gp.gmParticao.traducao(processoPedido.particao, processoPedido.contexto.regs[9])].opc = Opcode.DATA;
                    }

                    if (processoPedido.contexto.regs[8] == 2) { // OUT
                        System.out.println("Instrucao OUT do processo "+ processoPedido.getId()+ " : ");
                        vm.mem.dump(vm.m[gp.gmParticao.traducao(processoPedido.particao,processoPedido.contexto.pc)]);

                        int out = vm.m[gp.gmParticao.traducao(processoPedido.particao, processoPedido.contexto.regs[9])].p;
                        System.out.println("Output: " + out);
                    }

                    vm.cpu.irptIO.add(Interrupts.intIO);
                } else System.out.println("Sem nenhum pedido pendente");

                semaforoDaFilaDePedidos.release();
            } else {
                semaforoDaFilaDePedidos.acquireUninterruptibly();

                if(!filaPedidosPaginacao.isEmpty()){
                    var processoPedido = filaPedidosPaginacao.get(0);

                    if (processoPedido.contexto.regs[8] == 1) { //IN
                        System.out.println("Instrucao IN do processo "+ processoPedido.getId()+ " : ");
                        vm.mem.dump(vm.m[gp.gmPaginacao.traducao(processoPedido.tabelaDePaginas,processoPedido.contexto.pc)]);

                        Scanner scanner = new Scanner(System.in);
                        System.out.print("Digite o numero que deseja: ");

                        vm.m[gp.gmPaginacao.traducao(processoPedido.tabelaDePaginas, processoPedido.contexto.regs[9])].p = scanner.nextInt();
                        vm.m[gp.gmPaginacao.traducao(processoPedido.tabelaDePaginas, processoPedido.contexto.regs[9])].opc = Opcode.DATA;
                    }

                    if (processoPedido.contexto.regs[8] == 2) { // OUT
                        System.out.println("Instrucao OUT do processo "+ processoPedido.getId()+ " : ");
                        vm.mem.dump(vm.m[gp.gmPaginacao.traducao(processoPedido.tabelaDePaginas,processoPedido.contexto.pc)]);

                        int out = vm.m[gp.gmPaginacao.traducao(processoPedido.tabelaDePaginas, processoPedido.contexto.regs[9])].p;
                        System.out.println("Output: " + out);
                    }

                    vm.cpu.irptIO.add(Interrupts.intIO);
                }else System.out.println("Sem nenhum pedido pendente");

                semaforoDaFilaDePedidos.release();
            }
        }

        @Override
        public void run() {
            lidaPedido();
        }
    }

    public class InterfaceUsuario extends Thread{
        private Sistema s;

        public InterfaceUsuario(Sistema s){
            this.s = s;
        }

        @Override
        public void run() {
            boolean SystemRun = true;
            Scanner scanner = new Scanner(System.in);
            while (SystemRun) {

                System.out.println("Selecione o comando que deseja:");
                System.out.println("[1] Cria processo");
                System.out.println("[2] Cria multiplos processos");
                System.out.println("[3] Dump");
                System.out.println("[4] DumpM");
                System.out.println("[5] Lista processos");
                System.out.println("[6] Liga/Desliga dump de resultado");
                System.out.println("[7] Trace ON/Trace OFF");
                System.out.println("[8] ResponderPedidoIO");
                System.out.println("[0] Exit");

                String comando = scanner.nextLine();
                try {
                    switch (comando) {

                        case "1":
                            System.out.println("Selecione o programa que deseja");
                            System.out.println("[1] fatorial");
                            System.out.println("[2] fibonacci10");
                            System.out.println("[3] progMinimo");
                            System.out.println("[4] fatorialTRAP");
                            System.out.println("[5] PB");
                            System.out.println("[6] PC");
                            System.out.println("[7] fibonacciTRAP");
                            String programa = scanner.nextLine();
                            boolean result;
                            switch (programa) {
                                case "1" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "2" -> {
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "3" -> {
                                    result = s.gp.criaProcesso(progs.progMinimo);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "4" -> {
                                    result = s.gp.criaProcesso(progs.fatorialTRAP);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "5" -> {
                                    result = s.gp.criaProcesso(progs.PB);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "6" -> {
                                    result = s.gp.criaProcesso(progs.PC);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "7" -> {
                                    result = s.gp.criaProcesso(progs.fibonacciTRAP);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                default -> System.out.println("Opcao invalida");
                            }
                            break;
                        case "2":
                            System.out.println("Selecione o programa que deseja");
                            System.out.println("[1] 3 processos sem trap");
                            System.out.println("[2] 3 processos com 1 trap de in");
                            System.out.println("[3] 3 processos com 1 trap de out");
                            System.out.println("[4] 4 processos com 2 traps");
                            System.out.println("[5] Todos processos sem traps");
                            System.out.println("[6] Todos processos com trap");
                            programa = scanner.nextLine();

                            switch (programa) {
                                case "1" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    result = s.gp.criaProcesso(progs.progMinimo);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "2" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    result = s.gp.criaProcesso(progs.fatorialTRAP);
                                    result = s.gp.criaProcesso(progs.PB);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "3" -> {
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    result = s.gp.criaProcesso(progs.fibonacciTRAP);
                                    result = s.gp.criaProcesso(progs.progMinimo);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "4" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    result = s.gp.criaProcesso(progs.fatorialTRAP);
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    result = s.gp.criaProcesso(progs.fibonacciTRAP);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "5" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    result = s.gp.criaProcesso(progs.progMinimo);
                                    result = s.gp.criaProcesso(progs.PB);
                                    result = s.gp.criaProcesso(progs.PC);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                case "6" -> {
                                    result = s.gp.criaProcesso(progs.fatorial);
                                    result = s.gp.criaProcesso(progs.fatorialTRAP);
                                    result = s.gp.criaProcesso(progs.fibonacci10);
                                    result = s.gp.criaProcesso(progs.fibonacciTRAP);
                                    result = s.gp.criaProcesso(progs.progMinimo);
                                    result = s.gp.criaProcesso(progs.PB);
                                    result = s.gp.criaProcesso(progs.PC);
                                    System.out.println("Resultado da criacao: " + result);
                                }
                                default -> System.out.println("Opcao invalida");
                            }
                            break;
                        case "3":
                            System.out.println("Digite o ID do programa que deseja fazer Dump");
                            String idString = scanner.nextLine();
                            int id = Integer.parseInt(idString);
                            s.dumpProcesso(id);
                            break;
                        case "4":
                            int indiceInicial;
                            int indiceFinal;

                            System.out.println("Digite o indice inicial da memoria");
                            idString = scanner.nextLine();
                            indiceInicial = Integer.parseInt(idString);
                            System.out.println("Digite o indice final da memoria");
                            idString = scanner.nextLine();
                            indiceFinal = Integer.parseInt(idString);

                            s.vm.mem.dump(indiceInicial, indiceFinal);
                            break;
                        case "5":
                            s.listaProcessos();
                            break;
                        case "6":
                            if (s.vm.cpu.debugResposta){
                                System.out.println("Dump de resposta desligado");
                                s.vm.cpu.debugResposta = false;
                            }else {
                                System.out.println("Dump de resposta ligado");
                                s.vm.cpu.debugResposta = true;
                            }
                            break;
                        case "7":
                            if (s.vm.cpu.debug){
                                System.out.println("Trace OFF");
                                s.vm.cpu.debug = false;
                            }else {
                                System.out.println("Trace ON");
                                s.vm.cpu.debug = true;
                            }
                            break;
                        case "8":
                            System.out.println("Requisitando resposta...");
                            console.semaforoRespostaPedido.release();

                            console.semaforoLiberaInterface.acquireUninterruptibly();
                            break;
                        case "0":
                            System.out.println("Encerrando...");
                            SystemRun = false;
                            vm.cpu.interrupt();
                            console.interrupt();
                            break;
                        default:
                            System.out.println("Comando invalido, tente novamente");
                            break;
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }

            }
        }
    }

    // -------------------  S I S T E M A - fim --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------------------------
    // ------------------- instancia e testa sistema
    public static void main(String args[]) {
        Sistema s = new Sistema();
        //s.loadAndExec(progs.fibonacci10);
        //s.loadAndExec(progs.progMinimo);
        //s.loadAndExec(progs.fatorial);
        //s.loadAndExec(progs.fatorialTRAP); // saida
        //s.loadAndExec(progs.fibonacciTRAP); // entrada
        //s.loadAndExec(progs.PC); // bubble sort
    }


    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------------------
    // --------------- P R O G R A M A S  - não fazem parte do sistema
    // esta classe representa programas armazenados (como se estivessem em disco)
    // que podem ser carregados para a memória (load faz isto)

    public class Programas {
        public Word[] fatorial = new Word[] {
                // este fatorial so aceita valores positivos.   nao pode ser zero
                // linha   coment
                new Word(Opcode.LDI, 0, -1, 7),      // 0   	r0 é valor a calcular fatorial
                new Word(Opcode.LDI, 1, -1, 1),      // 1   	r1 é 1 para multiplicar (por r0)
                new Word(Opcode.LDI, 6, -1, 1),      // 2   	r6 é 1 para ser o decremento
                new Word(Opcode.LDI, 7, -1, 8),      // 3   	r7 tem posicao de stop do programa = 8
                new Word(Opcode.JMPIE, 7, 0, 0),     // 4   	se r0=0 pula para r7(=8)
                new Word(Opcode.MULT, 1, 0, -1),     // 5   	r1 = r1 * r0
                new Word(Opcode.SUB, 0, 6, -1),      // 6   	decrementa r0 1
                new Word(Opcode.JMP, -1, -1, 4),     // 7   	vai p posicao 4
                new Word(Opcode.STD, 1, -1, 10),     // 8   	coloca valor de r1 na posição 10
                new Word(Opcode.STOP, -1, -1, -1),   // 9   	stop
                new Word(Opcode.DATA, -1, -1, -1) }; // 10   ao final o valor do fatorial estará na posição 10 da memória

        public Word[] progMinimo = new Word[] {
                new Word(Opcode.LDI, 0, -1, 999),
                new Word(Opcode.STD, 0, -1, 10),
                new Word(Opcode.STD, 0, -1, 11),
                new Word(Opcode.STD, 0, -1, 12),
                new Word(Opcode.STD, 0, -1, 13),
                new Word(Opcode.STD, 0, -1, 14),
                new Word(Opcode.STOP, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1)
        };

        public Word[] fibonacci10 = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
                new Word(Opcode.LDI, 1, -1, 0),
                new Word(Opcode.STD, 1, -1, 20),
                new Word(Opcode.LDI, 2, -1, 1),
                new Word(Opcode.STD, 2, -1, 21),
                new Word(Opcode.LDI, 0, -1, 22),
                new Word(Opcode.LDI, 6, -1, 6),
                new Word(Opcode.LDI, 7, -1, 31),
                new Word(Opcode.LDI, 3, -1, 0),
                new Word(Opcode.ADD, 3, 1, -1),
                new Word(Opcode.LDI, 1, -1, 0),
                new Word(Opcode.ADD, 1, 2, -1),
                new Word(Opcode.ADD, 2, 3, -1),
                new Word(Opcode.STX, 0, 2, -1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.SUB, 7, 0, -1),
                new Word(Opcode.JMPIG, 6, 7, -1),
                new Word(Opcode.STOP, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),   // POS 20
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1) }; // ate aqui - serie de fibonacci ficara armazenada

        public Word[] fatorialTRAP = new Word[] {
                new Word(Opcode.LDI, 0, -1, 7),// numero para colocar na memoria
                new Word(Opcode.STD, 0, -1, 19), // correcao memoria / antes era 50 agora 19 para nao dar erro de endereco
                new Word(Opcode.LDD, 0, -1, 19),
                new Word(Opcode.LDI, 1, -1, -1),
                new Word(Opcode.LDI, 2, -1, 13),// SALVAR POS STOP
                new Word(Opcode.JMPIL, 2, 0, -1),// caso negativo pula pro STD
                new Word(Opcode.LDI, 1, -1, 1),
                new Word(Opcode.LDI, 6, -1, 1),
                new Word(Opcode.LDI, 7, -1, 13),
                new Word(Opcode.JMPIE, 7, 0, 0), //POS 9 pula pra STD (Stop-1)
                new Word(Opcode.MULT, 1, 0, -1),
                new Word(Opcode.SUB, 0, 6, -1),
                new Word(Opcode.JMP, -1, -1, 9),// pula para o JMPIE
                new Word(Opcode.STD, 1, -1, 18),
                new Word(Opcode.LDI, 8, -1, 2),// escrita
                new Word(Opcode.LDI, 9, -1, 18),//endereco com valor a escrever
                new Word(Opcode.TRAP, -1, -1, -1),
                new Word(Opcode.STOP, -1, -1, -1), // POS 17
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1) // numero armazenado
        };//POS 18


        public Word[] fibonacciTRAP = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
                new Word(Opcode.LDI, 8, -1, 1),// leitura
                new Word(Opcode.LDI, 9, -1, 56),//endereco a guardar
                new Word(Opcode.TRAP, -1, -1, -1),
                new Word(Opcode.LDD, 7, -1, 56),// numero do tamanho do fib
                new Word(Opcode.LDI, 3, -1, 0),
                new Word(Opcode.ADD, 3, 7, -1),
                new Word(Opcode.LDI, 4, -1, 36),//posicao para qual ira pular (stop) *
                new Word(Opcode.LDI, 1, -1, -1),// caso negativo
                new Word(Opcode.STD, 1, -1, 41),
                new Word(Opcode.JMPIL, 4, 7, -1),//pula pra stop caso negativo *
                new Word(Opcode.JMPIE, 4, 7, -1),//pula pra stop caso 0
                new Word(Opcode.ADDI, 7, -1, 41),// fibonacci + posição do stop
                new Word(Opcode.LDI, 1, -1, 0),
                new Word(Opcode.STD, 1, -1, 41),    // 25 posicao de memoria onde inicia a serie de fibonacci gerada
                new Word(Opcode.SUBI, 3, -1, 1),// se 1 pula pro stop
                new Word(Opcode.JMPIE, 4, 3, -1),
                new Word(Opcode.ADDI, 3, -1, 1),
                new Word(Opcode.LDI, 2, -1, 1),
                new Word(Opcode.STD, 2, -1, 42),
                new Word(Opcode.SUBI, 3, -1, 2),// se 2 pula pro stop
                new Word(Opcode.JMPIE, 4, 3, -1),
                new Word(Opcode.LDI, 0, -1, 43),
                new Word(Opcode.LDI, 6, -1, 25),// salva posição de retorno do loop
                new Word(Opcode.LDI, 5, -1, 0),//salva tamanho
                new Word(Opcode.ADD, 5, 7, -1),
                new Word(Opcode.LDI, 7, -1, 0),//zera (inicio do loop)
                new Word(Opcode.ADD, 7, 5, -1),//recarrega tamanho
                new Word(Opcode.LDI, 3, -1, 0),
                new Word(Opcode.ADD, 3, 1, -1),
                new Word(Opcode.LDI, 1, -1, 0),
                new Word(Opcode.ADD, 1, 2, -1),
                new Word(Opcode.ADD, 2, 3, -1),
                new Word(Opcode.STX, 0, 2, -1),
                new Word(Opcode.ADDI, 0, -1, 1),
                new Word(Opcode.SUB, 7, 0, -1),
                new Word(Opcode.JMPIG, 6, 7, -1),//volta para o inicio do loop
                new Word(Opcode.STOP, -1, -1, -1),   // POS 36
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),   // POS 41
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1)
        };

        public Word[] PB = new Word[] {
                //dado um inteiro em alguma posição de memória,
                // se for negativo armazena -1 na saída; se for positivo responde o fatorial do número na saída
                new Word(Opcode.LDI, 0, -1, 7),// numero para colocar na memoria
                new Word(Opcode.STD, 0, -1, 16), // correcao da memoria para nao dar endereco invalido
                new Word(Opcode.LDD, 0, -1, 16),
                new Word(Opcode.LDI, 1, -1, -1),
                new Word(Opcode.LDI, 2, -1, 13),// SALVAR POS STOP
                new Word(Opcode.JMPIL, 2, 0, -1),// caso negativo pula pro STD
                new Word(Opcode.LDI, 1, -1, 1),
                new Word(Opcode.LDI, 6, -1, 1),
                new Word(Opcode.LDI, 7, -1, 13),
                new Word(Opcode.JMPIE, 7, 0, 0), //POS 9 pula pra STD (Stop-1)
                new Word(Opcode.MULT, 1, 0, -1),
                new Word(Opcode.SUB, 0, 6, -1),
                new Word(Opcode.JMP, -1, -1, 9),// pula para o JMPIE
                new Word(Opcode.STD, 1, -1, 15),
                new Word(Opcode.STOP, -1, -1, -1), // POS 14
                new Word(Opcode.DATA, -1, -1, -1), //POS 15
                new Word(Opcode.DATA, -1, -1, -1)}; //numero armazenado

        public Word[] PC = new Word[] {
                //Para um N definido (10 por exemplo)
                //o programa ordena um vetor de N números em alguma posição de memória;
                //ordena usando bubble sort
                //loop ate que não swap nada
                //passando pelos N valores
                //faz swap de vizinhos se da esquerda maior que da direita
                new Word(Opcode.LDI, 7, -1, 5),// TAMANHO DO BUBBLE SORT (N)
                new Word(Opcode.LDI, 6, -1, 5),//aux N
                new Word(Opcode.LDI, 5, -1, 46),//LOCAL DA MEMORIA
                new Word(Opcode.LDI, 4, -1, 47),//aux local memoria
                new Word(Opcode.LDI, 0, -1, 4),//colocando valores na memoria
                new Word(Opcode.STD, 0, -1, 46),
                new Word(Opcode.LDI, 0, -1, 3),
                new Word(Opcode.STD, 0, -1, 47),
                new Word(Opcode.LDI, 0, -1, 5),
                new Word(Opcode.STD, 0, -1, 48),
                new Word(Opcode.LDI, 0, -1, 1),
                new Word(Opcode.STD, 0, -1, 49),
                new Word(Opcode.LDI, 0, -1, 2),
                new Word(Opcode.STD, 0, -1, 50),//colocando valores na memoria até aqui - POS 13
                new Word(Opcode.LDI, 3, -1, 25),// Posicao para pulo CHAVE 1
                new Word(Opcode.STD, 3, -1, 57),
                new Word(Opcode.LDI, 3, -1, 22),// Posicao para pulo CHAVE 2
                new Word(Opcode.STD, 3, -1, 56),
                new Word(Opcode.LDI, 3, -1, 38),// Posicao para pulo CHAVE 3
                new Word(Opcode.STD, 3, -1, 55),
                new Word(Opcode.LDI, 3, -1, 25),// Posicao para pulo CHAVE 4 (não usada)
                new Word(Opcode.STD, 3, -1, 54),
                new Word(Opcode.LDI, 6, -1, 0),//r6 = r7 - 1 POS 22
                new Word(Opcode.ADD, 6, 7, -1),
                new Word(Opcode.SUBI, 6, -1, 1),//ate aqui
                new Word(Opcode.JMPIEM, -1, 6, 55),//CHAVE 3 para pular quando r7 for 1 e r6 0 para interomper o loop de vez do programa
                new Word(Opcode.LDX, 0, 5, -1),//r0 e r1 pegando valores das posições da memoria POS 26
                new Word(Opcode.LDX, 1, 4, -1),
                new Word(Opcode.LDI, 2, -1, 0),
                new Word(Opcode.ADD, 2, 0, -1),
                new Word(Opcode.SUB, 2, 1, -1),
                new Word(Opcode.ADDI, 4, -1, 1),
                new Word(Opcode.SUBI, 6, -1, 1),
                new Word(Opcode.JMPILM, -1, 2, 57),//LOOP chave 1 caso neg procura prox
                new Word(Opcode.STX, 5, 1, -1),
                new Word(Opcode.SUBI, 4, -1, 1),
                new Word(Opcode.STX, 4, 0, -1),
                new Word(Opcode.ADDI, 4, -1, 1),
                new Word(Opcode.JMPIGM, -1, 6, 57),//LOOP chave 1 POS 38
                new Word(Opcode.ADDI, 5, -1, 1),
                new Word(Opcode.SUBI, 7, -1, 1),
                new Word(Opcode.LDI, 4, -1, 0),//r4 = r5 + 1 POS 41
                new Word(Opcode.ADD, 4, 5, -1),
                new Word(Opcode.ADDI, 4, -1, 1),//ate aqui
                new Word(Opcode.JMPIGM, -1, 7, 56),//LOOP chave 2
                new Word(Opcode.STOP, -1, -1, -1), // POS 45
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.___, -1, -1, -1), // para baixo jumps e para cima resposta
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1),
                new Word(Opcode.DATA, -1, -1, -1)
        };
    }
}

//public boolean desalocaProcesso(int id){
//		if(gmParticao!=null){
//		semaforoDeAcessoListaProntos.acquireUninterruptibly();
//
//		PCBparticao processoASerRemovido = null;
//		for (PCBparticao processo:listaDeProcessosProntosParticao) {
//		if(processo.getId() == id){
//		int enderecoLogico = processo.getParticao();
//		gmParticao.desaloca(enderecoLogico);
//
//		int enderecoFisicoInicio = gmParticao.traducao(processo.particao,0);
//		int enderecoFisicoFinal = enderecoFisicoInicio+gmParticao.tamPart;
//
//		for (int i = enderecoFisicoInicio;i< enderecoFisicoFinal;i++) {
//		gmParticao.memory.m[i] = new Word(Opcode.___, -1,-1,-1);
//		}
//		processoASerRemovido = processo;
//		}
//		}
//
//		if(processoASerRemovido!=null){
//		listaDeProcessosProntosParticao.remove(processoASerRemovido);
//		semaforoDeAcessoListaProntos.release();
//		return true;
//		} else {
//		semaforoDeAcessoListaProntos.release();
//		return false;
//		}
//
//		}else {
//		semaforoDeAcessoListaProntos.acquireUninterruptibly();
//
//		PCBpaginacao processoASerRemovido = null;
//		for (PCBpaginacao processo:listaDeProcessosProntosPaginacao) {
//		if(processo.getId() == id){
//		for (int pagina:processo.tabelaDePaginas) {
//		int enderecoInicial = pagina* gmPaginacao.tamFrames;
//		int enderecoFinal = enderecoInicial+ gmPaginacao.tamFrames-1;
//
//		for (int i = enderecoInicial;i<=enderecoFinal;i++){
//		gmPaginacao.memory.m[i] = new Word(Opcode.___, -1,-1,-1);
//		}
//		}
//
//
//
//		gmPaginacao.desalocaPaginas(processo.tabelaDePaginas);
//
//		processoASerRemovido = processo;
//		}
//		}
//		if(processoASerRemovido!=null){
//		listaDeProcessosProntosPaginacao.remove(processoASerRemovido);
//		semaforoDeAcessoListaProntos.release();
//		return true;
//		} else {
//		semaforoDeAcessoListaProntos.release();
//		return false;
//		}
//		}
//		}