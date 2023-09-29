// PUCRS - Escola Politécnica - Sistemas Operacionais
// Prof. Fernando Dotti
// Código fornecido como parte da solução do projeto de Sistemas Operacionais
//
// VM
//    HW = memória, cpu
//    SW = tratamento int e chamada de sistema
// Funcionalidades de carga, execução e dump de memória

import java.util.ArrayList;
import java.util.Scanner;

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
		noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow, intSTOP;
	}

	public class CPU {
		private int maxInt; // valores maximo e minimo para inteiros nesta cpu
		private int minInt;
							// característica do processador: contexto da CPU ...
		private int pc; 			// ... composto de program counter,
		private Word ir; 			// instruction register,
		private int[] reg;       	// registradores da CPU
		private Interrupts irpt; 	// durante instrucao, interrupcao pode ser sinalizada
		private int base;   		// base e limite de acesso na memoria
		private int limite; // por enquanto toda memoria pode ser acessada pelo processo rodando
							// ATE AQUI: contexto da CPU - tudo que precisa sobre o estado de um processo para executa-lo
							// nas proximas versoes isto pode modificar

		private Memory mem;               // mem tem funcoes de dump e o array m de memória 'fisica' 
		private Word[] m;                 // CPU acessa MEMORIA, guarda referencia a 'm'. m nao muda. semre será um array de palavras

		private InterruptHandling ih;     // significa desvio para rotinas de tratamento de  Int - se int ligada, desvia
        private SysCallHandling sysCall;  // significa desvio para tratamento de chamadas de sistema - trap 
		private boolean debug;            // se true entao mostra cada instrucao em execucao
						
		public CPU(Memory _mem, InterruptHandling _ih, SysCallHandling _sysCall, boolean _debug) {     // ref a MEMORIA e interrupt handler passada na criacao da CPU
			maxInt =  32767;        // capacidade de representacao modelada
			minInt = -32767;        // se exceder deve gerar interrupcao de overflow
			mem = _mem;	            // usa mem para acessar funcoes auxiliares (dump)
			m = mem.m; 				// usa o atributo 'm' para acessar a memoria.
			reg = new int[10]; 		// aloca o espaço dos registradores - regs 8 e 9 usados somente para IO
			ih = _ih;               // aponta para rotinas de tratamento de int
            sysCall = _sysCall;     // aponta para rotinas de tratamento de chamadas de sistema
			debug =  _debug;        // se true, print da instrucao em execucao
		}
		
		private boolean legal(int e) {                             // todo acesso a memoria tem que ser verificado
			return e <= limite;
		}

		private boolean testOverflow(int v) {                       // toda operacao matematica deve avaliar se ocorre overflow                      
			if ((v < minInt) || (v > maxInt)) {                       
				irpt = Interrupts.intOverflow;             
				return false;
			};
			return true;
		}
		
		public void setContext(int _base, int _limite, int _pc) {  // no futuro esta funcao vai ter que ser 
			base = _base;                                          // expandida para setar todo contexto de execucao,
			limite = _limite;									   // agora,  setamos somente os registradores base,
			pc = _pc;                                              // limite e pc (deve ser zero nesta versao)
			irpt = Interrupts.noInterrupt;                         // reset da interrupcao registrada
		}
		
		public void run() { 		// execucao da CPU supoe que o contexto da CPU, vide acima, esta devidamente setado			
			while (true) { 			// ciclo de instrucoes. acaba cfe instrucao, veja cada caso.
			   // --------------------------------------------------------------------------------------------------
			   // FETCH
				if (legal(pc)) { 	// pc valido
					ir = m[pc]; 	// <<<<<<<<<<<<           busca posicao da memoria apontada por pc, guarda em ir
					if (debug) { System.out.print("                               pc: "+pc+"       exec: ");  mem.dump(ir); }
			   // --------------------------------------------------------------------------------------------------
			   // EXECUTA INSTRUCAO NO ir
					switch (ir.opc) {   // conforme o opcode (código de operação) executa

					// Instrucoes de Busca e Armazenamento em Memoria
					    case LDI: // Rd ← k
							reg[ir.r1] = ir.p;
							pc++;
							break;

						case LDD: // Rd <- [A]
						    if (legal(ir.p)) {
							   reg[ir.r1] = m[ir.p].p;
							   pc++;
						    }
						    break;

						case LDX: // RD <- [RS] // NOVA
							if (legal(reg[ir.r2])) {
								reg[ir.r1] = m[reg[ir.r2]].p;
								pc++;
							}
							break;

						case STD: // [A] ← Rs
						    if (legal(ir.p)) {
							    m[ir.p].opc = Opcode.DATA;
							    m[ir.p].p = reg[ir.r1];
							    pc++;
							};
						    break;

						case STX: // [Rd] ←Rs
						    if (legal(reg[ir.r1])) {
							    m[reg[ir.r1]].opc = Opcode.DATA;      
							    m[reg[ir.r1]].p = reg[ir.r2];          
								pc++;
							};
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
	
						case JMPIM: // PC <- [A]
								 pc = m[ir.p].p;
							 break; 
	
						case JMPIGM: // If RC > 0 then PC <- [A] else PC++
								 if (reg[ir.r2] > 0) {
									pc = m[ir.p].p;
								} else {
									pc++;
								}
							 break;  
	
						case JMPILM: // If RC < 0 then PC <- k else PC++
								 if (reg[ir.r2] < 0) {
									pc = m[ir.p].p;
								} else {
									pc++;
								}
							 break; 
	
						case JMPIEM: // If RC = 0 then PC <- k else PC++
								if (reg[ir.r2] == 0) {
									pc = m[ir.p].p;
								} else {
									pc++;
								}
							 break; 
	
						case JMPIGT: // If RS>RC then PC <- k else PC++
								if (reg[ir.r1] > reg[ir.r2]) {
									pc = ir.p;
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
						     sysCall.handle();            // <<<<< aqui desvia para rotina de chamada de sistema, no momento so temos IO
							 pc++;
						     break;

					// Inexistente
						default:
							irpt = Interrupts.intInstrucaoInvalida;
							break;
					}
				}
				else break;
			   // --------------------------------------------------------------------------------------------------
			   // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
				if (!(irpt == Interrupts.noInterrupt)) {   // existe interrupção
					ih.handle(irpt,pc);                       // desvia para rotina de tratamento
					break; // break sai do loop da cpu
				}
			}  // FIM DO CICLO DE UMA INSTRUÇÃO
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
		public GP gp;

		public VM(InterruptHandling ih, SysCallHandling sysCall){
		 // vm deve ser configurada com endereço de tratamento de interrupcoes e de chamadas de sistema
	     // cria memória
		     tamMem = 1024;
  		 	 mem = new Memory(tamMem);
			 m = mem.m;
		// cria gerente de processos
		//PERGUNTAR PRO SOR SE EH AQUI!
			gp = new GP(new GMParticao(128,mem));
			//gp = new GP(new GMPaginacao(mem.tamMem,8,mem));
	  	 // cria cpu
			 cpu = new CPU(mem,ih,sysCall, true);                   // true liga debug
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
			}
	}

    // ------------------- C H A M A D A S  D E  S I S T E M A  - rotinas de tratamento ----------------------
    public class SysCallHandling {
        private VM vm;
        public void setVM(VM _vm){
            vm = _vm;
        }
        public void handle() {   // apenas avisa - todas interrupcoes neste momento finalizam o programa
            System.out.println("                                               Chamada de Sistema com op  /  par:  "+ vm.cpu.reg[8] + " / " + vm.cpu.reg[9]);
		}
    }

    // ------------------ U T I L I T A R I O S   D O   S I S T E M A -----------------------------------------
	// ------------------ load é invocado a partir de requisição do usuário

	private void loadProgram(Word[] p, Word[] m) {
		for (int i = 0; i < p.length; i++) {
			m[i].opc = p[i].opc;     m[i].r1 = p[i].r1;     m[i].r2 = p[i].r2;     m[i].p = p[i].p;
		}
	}

	private void loadProgram(Word[] p) {
		loadProgram(p, vm.m);
	}

	private void loadAndExec(Word[] p){
		loadProgram(p);    // carga do programa na memoria
				System.out.println("---------------------------------- programa carregado na memoria");
				vm.mem.dump(0, p.length);            // dump da memoria nestas posicoes				
		vm.cpu.setContext(0, vm.tamMem - 1, 0);      // seta estado da cpu ]
				System.out.println("---------------------------------- inicia execucao ");
		vm.cpu.run();                                // cpu roda programa ate parar	
				System.out.println("---------------------------------- memoria após execucao ");
				vm.mem.dump(0, p.length);            // dump da memoria com resultado
	}

	//funcao de execucao
	private void executa(int id){
		if(this.vm.gp.gmParticao!=null){
			var processo = this.vm.gp.listaDeProcessosProntosParticao.get(id);

			vm.cpu.setContext(0,processo.limit, processo.pc);
			vm.cpu.run();
		}else{
			var processo = this.vm.gp.listaDeProcessosProntosPaginacao.get(id);

			for (Pagina pagina:processo.tabelaDePaginas) {
				vm.cpu.setContext(0, pagina.limit, pagina.pc);
				vm.cpu.run();
			}
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
			//considerar tirar isso daqui para o GP
			//int numPart = aloca(programa.length);

			//retorno caso nao seja possivel alocar a memoria
			//if(numPart==-1)return -1;

			int enderecoFisico = traducao(enderecoLogico);
			var enderecoFisicoFinal = enderecoFisico + tamPart;
			var posicPrograma = 0;
			for (int i = enderecoFisico; i<enderecoFisicoFinal-1; i++){
				if(posicPrograma<programa.length-1){
					memory.m[i] = programa[posicPrograma];
				}else{
					//ver isso aqui, registradores
					memory.m[i] = new Word(Opcode.___, -1,-1,-1);
				}

				posicPrograma++;
			}
			return enderecoLogico;
		}

		public int traducao(int enderecoLogico){
			return enderecoLogico*tamPart;
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

		public ArrayList<Pagina> translate(ArrayList<Integer> tabelaDePaginas){
			ArrayList<Pagina> tabelaDePaginasTraduzida = new ArrayList<>();
			for (int enderecoLogico:tabelaDePaginas) {
				int enderecoFisico = enderecoLogico*tamFrames;
				int enderecoLimite = enderecoFisico+tamFrames;
				System.out.println(enderecoLimite);
				tabelaDePaginasTraduzida.add(new Pagina(enderecoLogico,enderecoFisico,enderecoLimite));
			}

			return tabelaDePaginasTraduzida;
		}

		public ArrayList<Pagina> carga(Word[] programa,ArrayList<Integer> tabelaDePaginas){
			ArrayList<Pagina> tabelaDePaginasTraduzida = translate(tabelaDePaginas);

			int posicaoPrograma=0;

			for (Pagina pagina: tabelaDePaginasTraduzida) {
				for(int i=pagina.pc;i<pagina.limit;i++){
					if(posicaoPrograma<programa.length){
						memory.m[i] = programa[posicaoPrograma];
						posicaoPrograma++;
					}else break;
				}
			}

			return tabelaDePaginasTraduzida;
		}
	}

	public class GP{
		public ArrayList<PCBparticao> listaDeProcessosProntosParticao = new ArrayList<>();

		public ArrayList<PCBpaginacao> listaDeProcessosProntosPaginacao = new ArrayList<>();
		public GMParticao gmParticao=null;
		public GMPaginacao gmPaginacao=null;

		public GP(GMParticao GM) {
			this.gmParticao = GM;
		}

		public GP(GMPaginacao GM) {
			this.gmPaginacao = GM;
		}

		public boolean criaProcesso(Word[] programa){

			if(gmParticao!=null){

				int enderecoLogico = gmParticao.aloca(programa.length);

				//programa maior que a particao
				if(enderecoLogico == -1)return false;
				else{
					int id = listaDeProcessosProntosParticao.size();
					int particao = gmParticao.carga(enderecoLogico,programa);
					int enderecoFisico = gmParticao.traducao(enderecoLogico);
					PCBparticao pcb = new PCBparticao(id,particao, enderecoFisico, enderecoFisico+ gmParticao.tamPart);
					listaDeProcessosProntosParticao.add(pcb);
					return true;
				}
			}else {
				ArrayList<Integer> tabelaDePaginas = new ArrayList<>();
				boolean temEspaco = gmPaginacao.aloca(programa.length, tabelaDePaginas);

				if(!temEspaco) return false;
				else{
					int id = listaDeProcessosProntosPaginacao.size();
					ArrayList<Pagina> tabelaDePaginasTraduzida = gmPaginacao.carga(programa,tabelaDePaginas);
					PCBpaginacao pcb = new PCBpaginacao(id,tabelaDePaginasTraduzida);
					listaDeProcessosProntosPaginacao.add(pcb);
					return true;
				}
			}
		}

		public void desalocaProcesso(int id){
			if(gmParticao!=null){
				for (PCBparticao processo:listaDeProcessosProntosParticao) {
					if(processo.getId() == id){
						int enderecoLogico = processo.getParticao();
						gmParticao.desaloca(enderecoLogico);

						System.out.println("aqui");

						//fazer desalocacao da memoria logica
						for (int i = (gmParticao.traducao(processo.particao));i< processo.limit;i++) {
							gmParticao.memory.m[i] = new Word(Opcode.___, -1,-1,-1);
						}

						System.out.println("aqui");
					}
				}
			}else {
				for (PCBpaginacao processo:listaDeProcessosProntosPaginacao) {
					if(processo.getId() == id){
						//implementar pag desaloca
					}
				}
			}
		}
	}

//	public class PCB {
//		private int id;
//		private String state;
//		private int particao;
//
//		private ArrayList<Integer> tabelaDePaginas;
//
//		public PCB(int id, int particao) {
//			this.id = id;
//			this.state = "Ready";
//			this.particao = particao;
//		}
//
//		public PCB(int id, ArrayList<Integer> tabelaDePaginas) {
//			this.id = id;
//			this.state = "Ready";
//			this.tabelaDePaginas = tabelaDePaginas;
//		}
//
//		public int getId() {
//			return id;
//		}
//
//		public void setId(int id) {
//			this.id = id;
//		}
//
//		public String getState() {
//			return state;
//		}
//
//		public void setState(String state) {
//			this.state = state;
//		}
//
//		public int getParticao() {
//			return particao;
//		}
//
//		public void setParticao(int particao) {
//			this.particao = particao;
//		}
//
//		public ArrayList<Integer> getTabelaDePaginas() {
//			return tabelaDePaginas;
//		}
//
//		public void setTabelaDePaginas(ArrayList<Integer> tabelaDePaginas) {
//			this.tabelaDePaginas = tabelaDePaginas;
//		}
//	}

	public class PCBparticao {
		private int id;
		private String state;
		private int particao;
		private int pc;
		private int limit;


		public PCBparticao(int id, int particao,int pc,int limit) {
			this.id = id;
			this.state = "Ready";
			this.particao = particao;
			this.pc = pc;
			this.limit = limit;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public int getParticao() {
			return particao;
		}

		public void setParticao(int particao) {
			this.particao = particao;
		}
	}

	public class PCBpaginacao {
		private int id;
		private String state;
		private ArrayList<Pagina> tabelaDePaginas;

		public PCBpaginacao(int id, ArrayList<Pagina> tabelaDePaginas) {
			this.id = id;
			this.state = "Ready";
			this.tabelaDePaginas = tabelaDePaginas;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public ArrayList<Pagina> getTabelaDePaginas() {
			return tabelaDePaginas;
		}

		public void setTabelaDePaginas(ArrayList<Pagina> tabelaDePaginas) {
			this.tabelaDePaginas = tabelaDePaginas;
		}
	}

	//para execucao
	public class Pagina{
		private int pagina;
		private int pc;
		private int limit;

		public Pagina(int pagina, int pc, int limit) {
			this.pagina = pagina;
			this.pc = pc;
			this.limit = limit;
		}

		public int getPagina() {
			return pagina;
		}

		public int getPc() {
			return pc;
		}

		public int getLimit() {
			return limit;
		}
	}


	// -------------------------------------------------------------------------------------------------------
    // -------------------  S I S T E M A --------------------------------------------------------------------

	public VM vm;
	public InterruptHandling ih;
	public SysCallHandling sysCall;
	public static Programas progs;

    public Sistema(){   // a VM com tratamento de interrupções
		 ih = new InterruptHandling();
         sysCall = new SysCallHandling();
		 vm = new VM(ih, sysCall);
		 sysCall.setVM(vm);
		 progs = new Programas();

	}

	public static class InterfaceUsuario {

		public InterfaceUsuario(Sistema s) {
			boolean SystemRun = true;
			Scanner scanner = new Scanner(System.in);
			while (SystemRun) {

				System.out.println("Selecione o comando que deseja:");
				System.out.println("[1] Cria processo");
				System.out.println("[2] Executa");
				System.out.println("[3] Dump");
				System.out.println("[4] DumpM");
				System.out.println("[5] Lista processos");
				System.out.println("[6] Desaloca");
				System.out.println("[7] Trace ON/Trace OFF");
				System.out.println("[0] Exit");

				String comando = scanner.nextLine();
				try {
					switch (comando) {

						case "1":
							System.out.println("Selecione o programa que deseja");
							System.out.println("[1] fatorial");
							System.out.println("[2] fatorialTRAP");
							System.out.println("[3] progMinimo");
							System.out.println("[4] fibonacci10");
							System.out.println("[5] PC");
							String programa = scanner.nextLine();
							boolean result;
							switch (programa) {
								case "1":
									result = s.vm.gp.criaProcesso(progs.fatorial);
									System.out.println("Resultado da criacao:" + result);
									break;
								case "2":
									result = s.vm.gp.criaProcesso(progs.fatorialTRAP);
									System.out.println("Resultado da criacao:" + result);
									break;
								case "3":
									result = s.vm.gp.criaProcesso(progs.progMinimo);
									System.out.println("Resultado da criacao:" + result);
									break;
								case "4":
									result = s.vm.gp.criaProcesso(progs.fibonacci10);
									System.out.println("Resultado da criacao:" + result);
									break;
								case "5":
									result = s.vm.gp.criaProcesso(progs.PC);
									System.out.println("Resultado da criacao:" + result);
									break;
								default:
									System.out.println("Opcao invalida");
									break;
							}
							break;
						case "2":
							System.out.println("Digite o ID do programa que deseja executar");
							String idString = scanner.nextLine();
							int id = Integer.parseInt(idString);
							s.executa(id);
							break;
						case "3":
							System.out.println("Digite o ID do programa que deseja fazer Dump");
							idString = scanner.nextLine();
							id = Integer.parseInt(idString);
							var processo = s.vm.gp.listaDeProcessosProntosPaginacao.get(id);
							s.vm.mem.dump(processo.tabelaDePaginas.get(0).pc, processo.tabelaDePaginas.get(0).getLimit());
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
						case "lista-processos":
							
							break;
						case "6":
							System.out.println("Digite o id do processo");
							idString = scanner.nextLine();
							id = Integer.parseInt(idString);

							s.vm.gp.desalocaProcesso(id);
							break;
						case "TraceON/OFF":

							break;
						case "0":
							System.out.println("Encerrando...");
							SystemRun = false;
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
		new InterfaceUsuario(s);
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
	 		new Word(Opcode.LDI, 0, -1, 4),      // 0   	r0 é valor a calcular fatorial
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
			new Word(Opcode.STOP, -1, -1, -1) };

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
		   new Word(Opcode.STD, 0, -1, 50),
		   new Word(Opcode.LDD, 0, -1, 50),
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
           new Word(Opcode.DATA, -1, -1, -1)  };//POS 18	
		   
	       public Word[] fibonacciTRAP = new Word[] { // mesmo que prog exemplo, so que usa r0 no lugar de r8
			new Word(Opcode.LDI, 8, -1, 1),// leitura
			new Word(Opcode.LDI, 9, -1, 100),//endereco a guardar
			new Word(Opcode.TRAP, -1, -1, -1),
			new Word(Opcode.LDD, 7, -1, 100),// numero do tamanho do fib
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
			new Word(Opcode.DATA, -1, -1, -1)
	};

	public Word[] PB = new Word[] {
		//dado um inteiro em alguma posição de memória,
		// se for negativo armazena -1 na saída; se for positivo responde o fatorial do número na saída
		new Word(Opcode.LDI, 0, -1, 7),// numero para colocar na memoria
		new Word(Opcode.STD, 0, -1, 50),
		new Word(Opcode.LDD, 0, -1, 50),
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
		new Word(Opcode.DATA, -1, -1, -1)}; //POS 15

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
		new Word(Opcode.STD, 3, -1, 99),
		new Word(Opcode.LDI, 3, -1, 22),// Posicao para pulo CHAVE 2
		new Word(Opcode.STD, 3, -1, 98),
		new Word(Opcode.LDI, 3, -1, 38),// Posicao para pulo CHAVE 3
		new Word(Opcode.STD, 3, -1, 97),
		new Word(Opcode.LDI, 3, -1, 25),// Posicao para pulo CHAVE 4 (não usada)
		new Word(Opcode.STD, 3, -1, 96),
		new Word(Opcode.LDI, 6, -1, 0),//r6 = r7 - 1 POS 22
		new Word(Opcode.ADD, 6, 7, -1),
		new Word(Opcode.SUBI, 6, -1, 1),//ate aqui
		new Word(Opcode.JMPIEM, -1, 6, 97),//CHAVE 3 para pular quando r7 for 1 e r6 0 para interomper o loop de vez do programa
		new Word(Opcode.LDX, 0, 5, -1),//r0 e r1 pegando valores das posições da memoria POS 26
		new Word(Opcode.LDX, 1, 4, -1),
		new Word(Opcode.LDI, 2, -1, 0),
		new Word(Opcode.ADD, 2, 0, -1),
		new Word(Opcode.SUB, 2, 1, -1),
		new Word(Opcode.ADDI, 4, -1, 1),
		new Word(Opcode.SUBI, 6, -1, 1),
		new Word(Opcode.JMPILM, -1, 2, 99),//LOOP chave 1 caso neg procura prox
		new Word(Opcode.STX, 5, 1, -1),
		new Word(Opcode.SUBI, 4, -1, 1),
		new Word(Opcode.STX, 4, 0, -1),
		new Word(Opcode.ADDI, 4, -1, 1),
		new Word(Opcode.JMPIGM, -1, 6, 99),//LOOP chave 1 POS 38
		new Word(Opcode.ADDI, 5, -1, 1),
		new Word(Opcode.SUBI, 7, -1, 1),
		new Word(Opcode.LDI, 4, -1, 0),//r4 = r5 + 1 POS 41
		new Word(Opcode.ADD, 4, 5, -1),
		new Word(Opcode.ADDI, 4, -1, 1),//ate aqui
		new Word(Opcode.JMPIGM, -1, 7, 98),//LOOP chave 2
		new Word(Opcode.STOP, -1, -1, -1), // POS 45
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1),
		new Word(Opcode.DATA, -1, -1, -1)};
   }
}

