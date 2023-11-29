# GustavoAvelineRocha-LeonardoPreczevskiRamos-RodrigoRosaRenck

Alunos: GustavoAvelineRocha,LeonardoPreczevskiRamos,RodrigoRosaRenck

O código esta completo, com todas as funcionalidades implementeadas.

# Casos de teste:

Criamos vários tipos de testes no menu, tendo em vista que, ao usuário pedir para criar um processo, ele rodará imediatamente. Fica difícil verificar a concorrência. Desse modo, existe a opção para criar diversos processos de uma só vez, sendo eles:

[1] 3 processos sem trap
[2] 3 processos com 1 trap de in
[3] 3 processos com 1 trap de out
[4] 4 processos com 2 traps
[5] Todos processos sem traps
[6] Todos processos com trap

Os processos com trap devem ser respondidos pelo usuário pelo número 8 no menu, para que eles sejam finalizados.

[8] Responder Pedido IO

Outro ponto importante a se falar dos testes são as opções 6 e 7, sendo elas:

[6] Liga/Desliga dump de resultado
[7] Trace ON/Trace OFF

A máquina virtual, por definição, vem com o dump do resultado e o trace (cada comando executado pela máquina ser printado) desligados. Assim sendo, é possível apenas ver as interrupções de escalonamento, stop, IO, etc.

Dessa forma, caso seja de interesse ver o resultado de cada processo, deve-se ligar o dump do resultado, que printa na tela o processo antes dele ser desalocado pela interrupção stop. Além disso, caso queira ver cada linha de código executada, também é possível ver com trace on ligado.

Agora irei deixar o resultado esperado de cada execução, concorrentemente com o dump de resultado ligado.

Observação 1: A ordem dos prints podem mudar dependendo da execução, pois se tratando de um processo concorrente, talvez os prints possam ser alterados, todavia o dump da memória será o mesmo. Observação 2: Os resultados serão printados usando o Gerente de memória de paginação, todavia, também é possível usar o gerente de memória de partição; isso apenas irá diferenciar o dump da memória.

# Caso 1: 

                                               Interrupcao intTimeSliceFinish   pc: 5   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 2
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intSTOP   pc: 6   id: 2
Id do processo = 2
Estado do processo = finalizado
Pagina do processo = 6
Pagina do processo = 7
Conteudo do processo:
48:  [ LDI, 0, -1, 999  ] 
49:  [ STD, 0, -1, 10  ] 
50:  [ STD, 0, -1, 11  ] 
51:  [ STD, 0, -1, 12  ] 
52:  [ STD, 0, -1, 13  ] 
53:  [ STD, 0, -1, 14  ] 
54:  [ STOP, -1, -1, -1  ] 
55:  [ DATA, -1, -1, -1  ] 
56:  [ DATA, -1, -1, -1  ] 
57:  [ DATA, -1, -1, -1  ] 
58:  [ DATA, -1, -1, 999  ] 
59:  [ DATA, -1, -1, 999  ] 
60:  [ DATA, -1, -1, 999  ] 
61:  [ DATA, -1, -1, 999  ] 
62:  [ DATA, -1, -1, 999  ] 
63:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 7   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 4   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 0
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intSTOP   pc: 9   id: 0

Id do processo = 0
Estado do processo = finalizado
Pagina do processo = 0
Pagina do processo = 1
Conteudo do processo:
0:  [ LDI, 0, -1, 7  ] 
1:  [ LDI, 1, -1, 1  ] 
2:  [ LDI, 6, -1, 1  ] 
3:  [ LDI, 7, -1, 8  ] 
4:  [ JMPIE, 7, 0, 0  ] 
5:  [ MULT, 1, 0, -1  ] 
6:  [ SUB, 0, 6, -1  ] 
7:  [ JMP, -1, -1, 4  ] 
8:  [ STD, 1, -1, 10  ] 
9:  [ STOP, -1, -1, -1  ] 
10:  [ DATA, -1, -1, 5040  ] 
11:  [ ___, -1, -1, -1  ] 
12:  [ ___, -1, -1, -1  ] 
13:  [ ___, -1, -1, -1  ] 
14:  [ ___, -1, -1, -1  ] 
15:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 1
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 1
                                               Interrupcao intSTOP   pc: 16   id: 1

Id do processo = 1
Estado do processo = finalizado
Pagina do processo = 2
Pagina do processo = 3
Pagina do processo = 4
Pagina do processo = 5
Conteudo do processo:
16:  [ LDI, 1, -1, 0  ] 
17:  [ STD, 1, -1, 20  ] 
18:  [ LDI, 2, -1, 1  ] 
19:  [ STD, 2, -1, 21  ] 
20:  [ LDI, 0, -1, 22  ] 
21:  [ LDI, 6, -1, 6  ] 
22:  [ LDI, 7, -1, 31  ] 
23:  [ LDI, 3, -1, 0  ] 
24:  [ ADD, 3, 1, -1  ] 
25:  [ LDI, 1, -1, 0  ] 
26:  [ ADD, 1, 2, -1  ] 
27:  [ ADD, 2, 3, -1  ] 
28:  [ STX, 0, 2, -1  ] 
29:  [ ADDI, 0, -1, 1  ] 
30:  [ SUB, 7, 0, -1  ] 
31:  [ JMPIG, 6, 7, -1  ] 
32:  [ STOP, -1, -1, -1  ] 
33:  [ DATA, -1, -1, -1  ] 
34:  [ DATA, -1, -1, -1  ] 
35:  [ DATA, -1, -1, -1  ] 
36:  [ DATA, -1, -1, 0  ] 
37:  [ DATA, -1, -1, 1  ] 
38:  [ DATA, -1, -1, 1  ] 
39:  [ DATA, -1, -1, 2  ] 
40:  [ DATA, -1, -1, 3  ] 
41:  [ DATA, -1, -1, 5  ] 
42:  [ DATA, -1, -1, 8  ] 
43:  [ DATA, -1, -1, 13  ] 
44:  [ DATA, -1, -1, 21  ] 
45:  [ DATA, -1, -1, 34  ] 
46:  [ DATA, -1, -1, 55  ] 
47:  [ ___, -1, -1, -1  ] 

# Caso 2:                                        
                                              Interrupcao intTimeSliceFinish   pc: 5   id: 3  
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 3
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 7   id: 3
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 4   id: 3
                                               Interrupcao intTimeSliceFinish   pc: 12   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 12   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 3
                                               Interrupcao intTimeSliceFinish   pc: 9   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 9   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 3
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 5
                                               Interrupcao intSTOP   pc: 9   id: 3

Id do processo = 3
Estado do processo = finalizado
Pagina do processo = 0
Pagina do processo = 1
Conteudo do processo:
0:  [ LDI, 0, -1, 7  ] 
1:  [ LDI, 1, -1, 1  ] 
2:  [ LDI, 6, -1, 1  ] 
3:  [ LDI, 7, -1, 8  ] 
4:  [ JMPIE, 7, 0, 0  ] 
5:  [ MULT, 1, 0, -1  ] 
6:  [ SUB, 0, 6, -1  ] 
7:  [ JMP, -1, -1, 4  ] 
8:  [ STD, 1, -1, 10  ] 
9:  [ STOP, -1, -1, -1  ] 
10:  [ DATA, -1, -1, 5040  ] 
11:  [ ___, -1, -1, -1  ] 
12:  [ ___, -1, -1, -1  ] 
13:  [ ___, -1, -1, -1  ] 
14:  [ ___, -1, -1, -1  ] 
15:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 4
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 5
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 4
                                               Interrupcao intSTOP   pc: 14   id: 5

Id do processo = 5
Estado do processo = finalizado
Pagina do processo = 5
Pagina do processo = 6
Pagina do processo = 7
Conteudo do processo:
40:  [ LDI, 0, -1, 7  ] 
41:  [ STD, 0, -1, 16  ] 
42:  [ LDD, 0, -1, 16  ] 
43:  [ LDI, 1, -1, -1  ] 
44:  [ LDI, 2, -1, 13  ] 
45:  [ JMPIL, 2, 0, -1  ] 
46:  [ LDI, 1, -1, 1  ] 
47:  [ LDI, 6, -1, 1  ] 
48:  [ LDI, 7, -1, 13  ] 
49:  [ JMPIE, 7, 0, 0  ] 
50:  [ MULT, 1, 0, -1  ] 
51:  [ SUB, 0, 6, -1  ] 
52:  [ JMP, -1, -1, 9  ] 
53:  [ STD, 1, -1, 15  ] 
54:  [ STOP, -1, -1, -1  ] 
55:  [ DATA, -1, -1, 5040  ] 
56:  [ DATA, -1, -1, 7  ] 
57:  [ ___, -1, -1, -1  ] 
58:  [ ___, -1, -1, -1  ] 
59:  [ ___, -1, -1, -1  ] 
60:  [ ___, -1, -1, -1  ] 
61:  [ ___, -1, -1, -1  ] 
62:  [ ___, -1, -1, -1  ] 
63:  [ ___, -1, -1, -1  ] 
                                               Chamada de Sistema com op  /  par:  2 / 18  ID: 4

8                                              //AQUI RESPONDENDO CHAMADA DE SISTEMA!
Requisitando resposta...
Instrucao OUT do processo 4 : 
[ TRAP, -1, -1, -1  ] 
Output: 5040
Selecione o comando que deseja:
[1] Cria processo
[2] Cria multiplos processos
[3] Dump
[4] DumpM
[5] Lista processos
[6] Liga/Desliga dump de resultado
[7] Trace ON/Trace OFF
[8] Responder Pedido IO
[0] Exit
                                               Interrupcao intIO   pc: 0
                                               Interrupcao intSTOP   pc: 17   id: 4

Id do processo = 4
Estado do processo = finalizado
Pagina do processo = 2
Pagina do processo = 3
Pagina do processo = 4
Conteudo do processo:
16:  [ LDI, 0, -1, 7  ] 
17:  [ STD, 0, -1, 19  ] 
18:  [ LDD, 0, -1, 19  ] 
19:  [ LDI, 1, -1, -1  ] 
20:  [ LDI, 2, -1, 13  ] 
21:  [ JMPIL, 2, 0, -1  ] 
22:  [ LDI, 1, -1, 1  ] 
23:  [ LDI, 6, -1, 1  ] 
24:  [ LDI, 7, -1, 13  ] 
25:  [ JMPIE, 7, 0, 0  ] 
26:  [ MULT, 1, 0, -1  ] 
27:  [ SUB, 0, 6, -1  ] 
28:  [ JMP, -1, -1, 9  ] 
29:  [ STD, 1, -1, 18  ] 
30:  [ LDI, 8, -1, 2  ] 
31:  [ LDI, 9, -1, 18  ] 
32:  [ TRAP, -1, -1, -1  ] 
33:  [ STOP, -1, -1, -1  ] 
34:  [ DATA, -1, -1, 5040  ] 
35:  [ DATA, -1, -1, 7  ] 
36:  [ ___, -1, -1, -1  ] 
37:  [ ___, -1, -1, -1  ] 
38:  [ ___, -1, -1, -1  ] 
39:  [ ___, -1, -1, -1  ] 

# Caso 3:

                                               Chamada de Sistema com op  /  par:  1 / 56  ID: 7
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 8
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intSTOP   pc: 6   id: 8

Id do processo = 8
Estado do processo = finalizado
Pagina do processo = 12
Pagina do processo = 13
Conteudo do processo:
96:  [ LDI, 0, -1, 999  ] 
97:  [ STD, 0, -1, 10  ] 
98:  [ STD, 0, -1, 11  ] 
99:  [ STD, 0, -1, 12  ] 
100:  [ STD, 0, -1, 13  ] 
101:  [ STD, 0, -1, 14  ] 
102:  [ STOP, -1, -1, -1  ] 
103:  [ DATA, -1, -1, -1  ] 
104:  [ DATA, -1, -1, -1  ] 
105:  [ DATA, -1, -1, -1  ] 
106:  [ DATA, -1, -1, 999  ] 
107:  [ DATA, -1, -1, 999  ] 
108:  [ DATA, -1, -1, 999  ] 
109:  [ DATA, -1, -1, 999  ] 
110:  [ DATA, -1, -1, 999  ] 
111:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 6
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 6
                                               Interrupcao intSTOP   pc: 16   id: 6

Id do processo = 6
Estado do processo = finalizado
Pagina do processo = 0
Pagina do processo = 1
Pagina do processo = 2
Pagina do processo = 3
Conteudo do processo:
0:  [ LDI, 1, -1, 0  ] 
1:  [ STD, 1, -1, 20  ] 
2:  [ LDI, 2, -1, 1  ] 
3:  [ STD, 2, -1, 21  ] 
4:  [ LDI, 0, -1, 22  ] 
5:  [ LDI, 6, -1, 6  ] 
6:  [ LDI, 7, -1, 31  ] 
7:  [ LDI, 3, -1, 0  ] 
8:  [ ADD, 3, 1, -1  ] 
9:  [ LDI, 1, -1, 0  ] 
10:  [ ADD, 1, 2, -1  ] 
11:  [ ADD, 2, 3, -1  ] 
12:  [ STX, 0, 2, -1  ] 
13:  [ ADDI, 0, -1, 1  ] 
14:  [ SUB, 7, 0, -1  ] 
15:  [ JMPIG, 6, 7, -1  ] 
16:  [ STOP, -1, -1, -1  ] 
17:  [ DATA, -1, -1, -1  ] 
18:  [ DATA, -1, -1, -1  ] 
19:  [ DATA, -1, -1, -1  ] 
20:  [ DATA, -1, -1, 0  ] 
21:  [ DATA, -1, -1, 1  ] 
22:  [ DATA, -1, -1, 1  ] 
23:  [ DATA, -1, -1, 2  ] 
24:  [ DATA, -1, -1, 3  ] 
25:  [ DATA, -1, -1, 5  ] 
26:  [ DATA, -1, -1, 8  ] 
27:  [ DATA, -1, -1, 13  ] 
28:  [ DATA, -1, -1, 21  ] 
29:  [ DATA, -1, -1, 34  ] 
30:  [ DATA, -1, -1, 55  ] 
31:  [ ___, -1, -1, -1  ] 
8                                              //AQUI RESPONDENDO CHAMADA DE SISTEMA!
Requisitando resposta...
Instrucao IN do processo 7 : 
[ TRAP, -1, -1, -1  ] 
Digite o numero que deseja: 11
Selecione o comando que deseja:
[1] Cria processo
[2] Cria multiplos processos
[3] Dump
[4] DumpM
[5] Lista processos
[6] Liga/Desliga dump de resultado
[7] Trace ON/Trace OFF
[8] Responder Pedido IO
[0] Exit
                                               Interrupcao intIO   pc: 0
                                               Interrupcao intTimeSliceFinish   pc: 8   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 13   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 18   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 23   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 29   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 34   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 7
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 7
                                               Interrupcao intSTOP   pc: 36   id: 7

Id do processo = 7
Estado do processo = finalizado
Pagina do processo = 4
Pagina do processo = 5
Pagina do processo = 6
Pagina do processo = 7
Pagina do processo = 8
Pagina do processo = 9
Pagina do processo = 10
Pagina do processo = 11
Conteudo do processo:
32:  [ LDI, 8, -1, 1  ] 
33:  [ LDI, 9, -1, 56  ] 
34:  [ TRAP, -1, -1, -1  ] 
35:  [ LDD, 7, -1, 56  ] 
36:  [ LDI, 3, -1, 0  ] 
37:  [ ADD, 3, 7, -1  ] 
38:  [ LDI, 4, -1, 36  ] 
39:  [ LDI, 1, -1, -1  ] 
40:  [ STD, 1, -1, 41  ] 
41:  [ JMPIL, 4, 7, -1  ] 
42:  [ JMPIE, 4, 7, -1  ] 
43:  [ ADDI, 7, -1, 41  ] 
44:  [ LDI, 1, -1, 0  ] 
45:  [ STD, 1, -1, 41  ] 
46:  [ SUBI, 3, -1, 1  ] 
47:  [ JMPIE, 4, 3, -1  ] 
48:  [ ADDI, 3, -1, 1  ] 
49:  [ LDI, 2, -1, 1  ] 
50:  [ STD, 2, -1, 42  ] 
51:  [ SUBI, 3, -1, 2  ] 
52:  [ JMPIE, 4, 3, -1  ] 
53:  [ LDI, 0, -1, 43  ] 
54:  [ LDI, 6, -1, 25  ] 
55:  [ LDI, 5, -1, 0  ] 
56:  [ ADD, 5, 7, -1  ] 
57:  [ LDI, 7, -1, 0  ] 
58:  [ ADD, 7, 5, -1  ] 
59:  [ LDI, 3, -1, 0  ] 
60:  [ ADD, 3, 1, -1  ] 
61:  [ LDI, 1, -1, 0  ] 
62:  [ ADD, 1, 2, -1  ] 
63:  [ ADD, 2, 3, -1  ] 
64:  [ STX, 0, 2, -1  ] 
65:  [ ADDI, 0, -1, 1  ] 
66:  [ SUB, 7, 0, -1  ] 
67:  [ JMPIG, 6, 7, -1  ] 
68:  [ STOP, -1, -1, -1  ] 
69:  [ DATA, -1, -1, -1  ] 
70:  [ DATA, -1, -1, -1  ] 
71:  [ DATA, -1, -1, -1  ] 
72:  [ DATA, -1, -1, -1  ] 
73:  [ DATA, -1, -1, 0  ] 
74:  [ DATA, -1, -1, 1  ] 
75:  [ DATA, -1, -1, 1  ] 
76:  [ DATA, -1, -1, 2  ] 
77:  [ DATA, -1, -1, 3  ] 
78:  [ DATA, -1, -1, 5  ] 
79:  [ DATA, -1, -1, 8  ] 
80:  [ DATA, -1, -1, 13  ] 
81:  [ DATA, -1, -1, 21  ] 
82:  [ DATA, -1, -1, 34  ] 
83:  [ DATA, -1, -1, 55  ] 
84:  [ DATA, -1, -1, -1  ] 
85:  [ DATA, -1, -1, -1  ] 
86:  [ DATA, -1, -1, -1  ] 
87:  [ DATA, -1, -1, -1  ] 
88:  [ DATA, -1, -1, 11  ] 
89:  [ ___, -1, -1, -1  ] 
90:  [ ___, -1, -1, -1  ] 
91:  [ ___, -1, -1, -1  ] 
92:  [ ___, -1, -1, -1  ]
93:  [ ___, -1, -1, -1  ] 
94:  [ ___, -1, -1, -1  ] 
95:  [ ___, -1, -1, -1  ] 

# caso 4: Igual aos casos 2 e 3 juntos, entretanto sendo necessario fazer duas vezes a resposta de IO.

# caso 5:

                                               Interrupcao intTimeSliceFinish   pc: 5   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 11
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intSTOP   pc: 6   id: 11

Id do processo = 11
Estado do processo = finalizado
Pagina do processo = 6
Pagina do processo = 7
Conteudo do processo:
48:  [ LDI, 0, -1, 999  ] 
49:  [ STD, 0, -1, 10  ] 
50:  [ STD, 0, -1, 11  ] 
51:  [ STD, 0, -1, 12  ] 
52:  [ STD, 0, -1, 13  ] 
53:  [ STD, 0, -1, 14  ] 
54:  [ STOP, -1, -1, -1  ] 
55:  [ DATA, -1, -1, -1  ] 
56:  [ DATA, -1, -1, -1  ] 
57:  [ DATA, -1, -1, -1  ] 
58:  [ DATA, -1, -1, 999  ] 
59:  [ DATA, -1, -1, 999  ] 
60:  [ DATA, -1, -1, 999  ] 
61:  [ DATA, -1, -1, 999  ] 
62:  [ DATA, -1, -1, 999  ] 
63:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 7   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 4   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 12   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 20   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 9   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 9
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 13
                                               Interrupcao intSTOP   pc: 9   id: 9

Id do processo = 9
Estado do processo = finalizado
Pagina do processo = 0
Pagina do processo = 1
Conteudo do processo:
0:  [ LDI, 0, -1, 7  ] 
1:  [ LDI, 1, -1, 1  ] 
2:  [ LDI, 6, -1, 1  ] 
3:  [ LDI, 7, -1, 8  ] 
4:  [ JMPIE, 7, 0, 0  ] 
5:  [ MULT, 1, 0, -1  ] 
6:  [ SUB, 0, 6, -1  ] 
7:  [ JMP, -1, -1, 4  ] 
8:  [ STD, 1, -1, 10  ] 
9:  [ STOP, -1, -1, -1  ] 
10:  [ DATA, -1, -1, 5040  ] 
11:  [ ___, -1, -1, -1  ] 
12:  [ ___, -1, -1, -1  ] 
13:  [ ___, -1, -1, -1  ] 
14:  [ ___, -1, -1, -1  ] 
15:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 12
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intSTOP   pc: 14   id: 12

Id do processo = 12
Estado do processo = finalizado
Pagina do processo = 8
Pagina do processo = 9
Pagina do processo = 10
Conteudo do processo:
64:  [ LDI, 0, -1, 7  ] 
65:  [ STD, 0, -1, 16  ] 
66:  [ LDD, 0, -1, 16  ] 
67:  [ LDI, 1, -1, -1  ] 
68:  [ LDI, 2, -1, 13  ] 
69:  [ JMPIL, 2, 0, -1  ] 
70:  [ LDI, 1, -1, 1  ] 
71:  [ LDI, 6, -1, 1  ] 
72:  [ LDI, 7, -1, 13  ] 
73:  [ JMPIE, 7, 0, 0  ] 
74:  [ MULT, 1, 0, -1  ] 
75:  [ SUB, 0, 6, -1  ] 
76:  [ JMP, -1, -1, 9  ] 
77:  [ STD, 1, -1, 15  ] 
78:  [ STOP, -1, -1, -1  ] 
79:  [ DATA, -1, -1, 5040  ] 
80:  [ DATA, -1, -1, 7  ] 
81:  [ ___, -1, -1, -1  ] 
82:  [ ___, -1, -1, -1  ] 
83:  [ ___, -1, -1, -1  ] 
84:  [ ___, -1, -1, -1  ] 
85:  [ ___, -1, -1, -1  ] 
86:  [ ___, -1, -1, -1  ] 
87:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 37   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 41   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 23   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 10
                                               Interrupcao intTimeSliceFinish   pc: 29   id: 13
                                               Interrupcao intSTOP   pc: 16   id: 10

Id do processo = 10
Estado do processo = finalizado
Pagina do processo = 2
Pagina do processo = 3
Pagina do processo = 4
Pagina do processo = 5
Conteudo do processo:
16:  [ LDI, 1, -1, 0  ] 
17:  [ STD, 1, -1, 20  ] 
18:  [ LDI, 2, -1, 1  ] 
19:  [ STD, 2, -1, 21  ] 
20:  [ LDI, 0, -1, 22  ] 
21:  [ LDI, 6, -1, 6  ] 
22:  [ LDI, 7, -1, 31  ] 
23:  [ LDI, 3, -1, 0  ] 
24:  [ ADD, 3, 1, -1  ] 
25:  [ LDI, 1, -1, 0  ] 
26:  [ ADD, 1, 2, -1  ] 
27:  [ ADD, 2, 3, -1  ] 
28:  [ STX, 0, 2, -1  ] 
29:  [ ADDI, 0, -1, 1  ] 
30:  [ SUB, 7, 0, -1  ] 
31:  [ JMPIG, 6, 7, -1  ] 
32:  [ STOP, -1, -1, -1  ] 
33:  [ DATA, -1, -1, -1  ] 
34:  [ DATA, -1, -1, -1  ] 
35:  [ DATA, -1, -1, -1  ] 
36:  [ DATA, -1, -1, 0  ] 
37:  [ DATA, -1, -1, 1  ] 
38:  [ DATA, -1, -1, 1  ] 
39:  [ DATA, -1, -1, 2  ] 
40:  [ DATA, -1, -1, 3  ] 
41:  [ DATA, -1, -1, 5  ] 
42:  [ DATA, -1, -1, 8  ] 
43:  [ DATA, -1, -1, 13  ] 
44:  [ DATA, -1, -1, 21  ] 
45:  [ DATA, -1, -1, 34  ] 
46:  [ DATA, -1, -1, 55  ] 
47:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 34   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 40   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 22   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 37   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 38   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 43   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 40   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 22   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 39   id: 13
                                               Interrupcao intTimeSliceFinish   pc: 44   id: 13
                                               Interrupcao intSTOP   pc: 45   id: 13

Id do processo = 13
Estado do processo = finalizado
Pagina do processo = 11
Pagina do processo = 12
Pagina do processo = 13
Pagina do processo = 14
Pagina do processo = 15
Pagina do processo = 16
Pagina do processo = 17
Pagina do processo = 18
Conteudo do processo:
88:  [ LDI, 7, -1, 5  ] 
89:  [ LDI, 6, -1, 5  ] 
90:  [ LDI, 5, -1, 46  ] 
91:  [ LDI, 4, -1, 47  ] 
92:  [ LDI, 0, -1, 4  ] 
93:  [ STD, 0, -1, 46  ] 
94:  [ LDI, 0, -1, 3  ] 
95:  [ STD, 0, -1, 47  ] 
96:  [ LDI, 0, -1, 5  ] 
97:  [ STD, 0, -1, 48  ] 
98:  [ LDI, 0, -1, 1  ] 
99:  [ STD, 0, -1, 49  ] 
100:  [ LDI, 0, -1, 2  ] 
101:  [ STD, 0, -1, 50  ] 
102:  [ LDI, 3, -1, 25  ] 
103:  [ STD, 3, -1, 57  ] 
104:  [ LDI, 3, -1, 22  ] 
105:  [ STD, 3, -1, 56  ] 
106:  [ LDI, 3, -1, 38  ] 
107:  [ STD, 3, -1, 55  ] 
108:  [ LDI, 3, -1, 25  ] 
109:  [ STD, 3, -1, 54  ] 
110:  [ LDI, 6, -1, 0  ] 
111:  [ ADD, 6, 7, -1  ] 
112:  [ SUBI, 6, -1, 1  ] 
113:  [ JMPIEM, -1, 6, 55  ] 
114:  [ LDX, 0, 5, -1  ] 
115:  [ LDX, 1, 4, -1  ] 
116:  [ LDI, 2, -1, 0  ] 
117:  [ ADD, 2, 0, -1  ] 
118:  [ SUB, 2, 1, -1  ] 
119:  [ ADDI, 4, -1, 1  ] 
120:  [ SUBI, 6, -1, 1  ] 
121:  [ JMPILM, -1, 2, 57  ] 
122:  [ STX, 5, 1, -1  ] 
123:  [ SUBI, 4, -1, 1  ] 
124:  [ STX, 4, 0, -1  ] 
125:  [ ADDI, 4, -1, 1  ] 
126:  [ JMPIGM, -1, 6, 57  ] 
127:  [ ADDI, 5, -1, 1  ] 
128:  [ SUBI, 7, -1, 1  ] 
129:  [ LDI, 4, -1, 0  ] 
130:  [ ADD, 4, 5, -1  ] 
131:  [ ADDI, 4, -1, 1  ] 
132:  [ JMPIGM, -1, 7, 56  ] 
133:  [ STOP, -1, -1, -1  ] 
134:  [ DATA, -1, -1, 1  ] 
135:  [ DATA, -1, -1, 2  ] 
136:  [ DATA, -1, -1, 3  ] 
137:  [ DATA, -1, -1, 4  ] 
138:  [ DATA, -1, -1, 5  ] 
139:  [ DATA, -1, -1, -1  ] 
140:  [ DATA, -1, -1, -1  ] 
141:  [ DATA, -1, -1, -1  ] 
142:  [ DATA, -1, -1, 25  ] 
143:  [ DATA, -1, -1, 38  ] 
144:  [ DATA, -1, -1, 22  ] 
145:  [ DATA, -1, -1, 25  ] 
146:  [ DATA, -1, -1, -1  ] 
147:  [ ___, -1, -1, -1  ] 
148:  [ ___, -1, -1, -1  ] 
149:  [ ___, -1, -1, -1  ] 
150:  [ ___, -1, -1, -1  ] 
151:  [ ___, -1, -1, -1  ] 

# Caso 6:

                                               Interrupcao intTimeSliceFinish   pc: 5   id: 16
                                               Chamada de Sistema com op  /  par:  1 / 56  ID: 17
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 18
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 14
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intSTOP   pc: 6   id: 18

Id do processo = 18
Estado do processo = finalizado
Pagina do processo = 17
Pagina do processo = 18
Conteudo do processo:
136:  [ LDI, 0, -1, 999  ] 
137:  [ STD, 0, -1, 10  ] 
138:  [ STD, 0, -1, 11  ] 
139:  [ STD, 0, -1, 12  ] 
140:  [ STD, 0, -1, 13  ] 
141:  [ STD, 0, -1, 14  ] 
142:  [ STOP, -1, -1, -1  ] 
143:  [ DATA, -1, -1, -1  ] 
144:  [ DATA, -1, -1, -1  ] 
145:  [ DATA, -1, -1, -1  ] 
146:  [ DATA, -1, -1, 999  ] 
147:  [ DATA, -1, -1, 999  ] 
148:  [ DATA, -1, -1, 999  ] 
149:  [ DATA, -1, -1, 999  ] 
150:  [ DATA, -1, -1, 999  ] 
151:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 7   id: 14
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 4   id: 14
                                               Interrupcao intTimeSliceFinish   pc: 12   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 12   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 20   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 5   id: 14
                                               Interrupcao intTimeSliceFinish   pc: 9   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 9   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 6   id: 14
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 20
                                               Interrupcao intSTOP   pc: 9   id: 14

Id do processo = 14
Estado do processo = finalizado
Pagina do processo = 0
Pagina do processo = 1
Conteudo do processo:
0:  [ LDI, 0, -1, 7  ] 
1:  [ LDI, 1, -1, 1  ] 
2:  [ LDI, 6, -1, 1  ] 
3:  [ LDI, 7, -1, 8  ] 
4:  [ JMPIE, 7, 0, 0  ] 
5:  [ MULT, 1, 0, -1  ] 
6:  [ SUB, 0, 6, -1  ] 
7:  [ JMP, -1, -1, 4  ] 
8:  [ STD, 1, -1, 10  ] 
9:  [ STOP, -1, -1, -1  ] 
10:  [ DATA, -1, -1, 5040  ] 
11:  [ ___, -1, -1, -1  ] 
12:  [ ___, -1, -1, -1  ] 
13:  [ ___, -1, -1, -1  ] 
14:  [ ___, -1, -1, -1  ] 
15:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 11   id: 19
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 15
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intSTOP   pc: 14   id: 19

Id do processo = 19
Estado do processo = finalizado
Pagina do processo = 19
Pagina do processo = 20
Pagina do processo = 21
Conteudo do processo:
152:  [ LDI, 0, -1, 7  ] 
153:  [ STD, 0, -1, 16  ] 
154:  [ LDD, 0, -1, 16  ] 
155:  [ LDI, 1, -1, -1  ] 
156:  [ LDI, 2, -1, 13  ] 
157:  [ JMPIL, 2, 0, -1  ] 
158:  [ LDI, 1, -1, 1  ] 
159:  [ LDI, 6, -1, 1  ] 
160:  [ LDI, 7, -1, 13  ] 
161:  [ JMPIE, 7, 0, 0  ] 
162:  [ MULT, 1, 0, -1  ] 
163:  [ SUB, 0, 6, -1  ] 
164:  [ JMP, -1, -1, 9  ] 
165:  [ STD, 1, -1, 15  ] 
166:  [ STOP, -1, -1, -1  ] 
167:  [ DATA, -1, -1, 5040  ] 
168:  [ DATA, -1, -1, 7  ] 
169:  [ ___, -1, -1, -1  ] 
170:  [ ___, -1, -1, -1  ] 
171:  [ ___, -1, -1, -1  ] 
172:  [ ___, -1, -1, -1  ] 
173:  [ ___, -1, -1, -1  ] 
174:  [ ___, -1, -1, -1  ] 
175:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 20
                                               Chamada de Sistema com op  /  par:  2 / 18  ID: 15
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 37   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 41   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 23   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 10   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 15   id: 16
                                               Interrupcao intTimeSliceFinish   pc: 29   id: 20
                                               Interrupcao intSTOP   pc: 16   id: 16

Id do processo = 16
Estado do processo = finalizado
Pagina do processo = 5
Pagina do processo = 6
Pagina do processo = 7
Pagina do processo = 8
Conteudo do processo:
40:  [ LDI, 1, -1, 0  ] 
41:  [ STD, 1, -1, 20  ] 
42:  [ LDI, 2, -1, 1  ] 
43:  [ STD, 2, -1, 21  ] 
44:  [ LDI, 0, -1, 22  ] 
45:  [ LDI, 6, -1, 6  ] 
46:  [ LDI, 7, -1, 31  ] 
47:  [ LDI, 3, -1, 0  ] 
48:  [ ADD, 3, 1, -1  ] 
49:  [ LDI, 1, -1, 0  ] 
50:  [ ADD, 1, 2, -1  ] 
51:  [ ADD, 2, 3, -1  ] 
52:  [ STX, 0, 2, -1  ] 
53:  [ ADDI, 0, -1, 1  ] 
54:  [ SUB, 7, 0, -1  ] 
55:  [ JMPIG, 6, 7, -1  ] 
56:  [ STOP, -1, -1, -1  ] 
57:  [ DATA, -1, -1, -1  ] 
58:  [ DATA, -1, -1, -1  ] 
59:  [ DATA, -1, -1, -1  ] 
60:  [ DATA, -1, -1, 0  ] 
61:  [ DATA, -1, -1, 1  ] 
62:  [ DATA, -1, -1, 1  ] 
63:  [ DATA, -1, -1, 2  ] 
64:  [ DATA, -1, -1, 3  ] 
65:  [ DATA, -1, -1, 5  ] 
66:  [ DATA, -1, -1, 8  ] 
67:  [ DATA, -1, -1, 13  ] 
68:  [ DATA, -1, -1, 21  ] 
69:  [ DATA, -1, -1, 34  ] 
70:  [ DATA, -1, -1, 55  ] 
71:  [ ___, -1, -1, -1  ] 
                                               Interrupcao intTimeSliceFinish   pc: 34   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 40   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 22   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 37   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 38   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 43   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 40   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 22   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 39   id: 20
                                               Interrupcao intTimeSliceFinish   pc: 44   id: 20
                                               Interrupcao intSTOP   pc: 45   id: 20

Id do processo = 20
Estado do processo = finalizado
Pagina do processo = 22
Pagina do processo = 23
Pagina do processo = 24
Pagina do processo = 25
Pagina do processo = 26
Pagina do processo = 27
Pagina do processo = 28
Pagina do processo = 29
Conteudo do processo:
176:  [ LDI, 7, -1, 5  ] 
177:  [ LDI, 6, -1, 5  ] 
178:  [ LDI, 5, -1, 46  ] 
179:  [ LDI, 4, -1, 47  ] 
180:  [ LDI, 0, -1, 4  ] 
181:  [ STD, 0, -1, 46  ] 
182:  [ LDI, 0, -1, 3  ] 
183:  [ STD, 0, -1, 47  ] 
184:  [ LDI, 0, -1, 5  ] 
185:  [ STD, 0, -1, 48  ] 
186:  [ LDI, 0, -1, 1  ] 
187:  [ STD, 0, -1, 49  ] 
188:  [ LDI, 0, -1, 2  ] 
189:  [ STD, 0, -1, 50  ] 
190:  [ LDI, 3, -1, 25  ] 
191:  [ STD, 3, -1, 57  ] 
192:  [ LDI, 3, -1, 22  ] 
193:  [ STD, 3, -1, 56  ] 
194:  [ LDI, 3, -1, 38  ] 
195:  [ STD, 3, -1, 55  ] 
196:  [ LDI, 3, -1, 25  ] 
197:  [ STD, 3, -1, 54  ] 
198:  [ LDI, 6, -1, 0  ] 
199:  [ ADD, 6, 7, -1  ] 
200:  [ SUBI, 6, -1, 1  ] 
201:  [ JMPIEM, -1, 6, 55  ] 
202:  [ LDX, 0, 5, -1  ] 
203:  [ LDX, 1, 4, -1  ] 
204:  [ LDI, 2, -1, 0  ] 
205:  [ ADD, 2, 0, -1  ] 
206:  [ SUB, 2, 1, -1  ] 
207:  [ ADDI, 4, -1, 1  ] 
208:  [ SUBI, 6, -1, 1  ] 
209:  [ JMPILM, -1, 2, 57  ] 
210:  [ STX, 5, 1, -1  ] 
211:  [ SUBI, 4, -1, 1  ] 
212:  [ STX, 4, 0, -1  ] 
213:  [ ADDI, 4, -1, 1  ] 
214:  [ JMPIGM, -1, 6, 57  ] 
215:  [ ADDI, 5, -1, 1  ] 
216:  [ SUBI, 7, -1, 1  ] 
217:  [ LDI, 4, -1, 0  ] 
218:  [ ADD, 4, 5, -1  ] 
219:  [ ADDI, 4, -1, 1  ] 
220:  [ JMPIGM, -1, 7, 56  ] 
221:  [ STOP, -1, -1, -1  ] 
222:  [ DATA, -1, -1, 1  ] 
223:  [ DATA, -1, -1, 2  ] 
224:  [ DATA, -1, -1, 3  ] 
225:  [ DATA, -1, -1, 4  ] 
226:  [ DATA, -1, -1, 5  ] 
227:  [ DATA, -1, -1, -1  ] 
228:  [ DATA, -1, -1, -1  ] 
229:  [ DATA, -1, -1, -1  ] 
230:  [ DATA, -1, -1, 25  ] 
231:  [ DATA, -1, -1, 38  ] 
232:  [ DATA, -1, -1, 22  ] 
233:  [ DATA, -1, -1, 25  ] 
234:  [ DATA, -1, -1, -1  ] 
235:  [ ___, -1, -1, -1  ] 
236:  [ ___, -1, -1, -1  ] 
237:  [ ___, -1, -1, -1  ] 
238:  [ ___, -1, -1, -1  ] 
239:  [ ___, -1, -1, -1  ] 
8                                              //AQUI RESPONDENDO 1 CHAMADA DE SISTEMA
Requisitando resposta...
Instrucao IN do processo 17 : 
[ TRAP, -1, -1, -1  ] 
Digite o numero que deseja: 11
Selecione o comando que deseja:
[1] Cria processo
[2] Cria multiplos processos
[3] Dump
[4] DumpM
[5] Lista processos
[6] Liga/Desliga dump de resultado
[7] Trace ON/Trace OFF
[8] Responder Pedido IO
[0] Exit
                                               Interrupcao intIO   pc: 0
                                               Interrupcao intTimeSliceFinish   pc: 8   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 13   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 18   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 23   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 29   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 34   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 28   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 33   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 27   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 32   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 26   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 31   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 25   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 30   id: 17
                                               Interrupcao intTimeSliceFinish   pc: 35   id: 17
                                               Interrupcao intSTOP   pc: 36   id: 17

Id do processo = 17
Estado do processo = finalizado
Pagina do processo = 9
Pagina do processo = 10
Pagina do processo = 11
Pagina do processo = 12
Pagina do processo = 13
Pagina do processo = 14
Pagina do processo = 15
Pagina do processo = 16
Conteudo do processo:
72:  [ LDI, 8, -1, 1  ] 
73:  [ LDI, 9, -1, 56  ] 
74:  [ TRAP, -1, -1, -1  ] 
75:  [ LDD, 7, -1, 56  ] 
76:  [ LDI, 3, -1, 0  ] 
77:  [ ADD, 3, 7, -1  ] 
78:  [ LDI, 4, -1, 36  ] 
79:  [ LDI, 1, -1, -1  ] 
80:  [ STD, 1, -1, 41  ] 
81:  [ JMPIL, 4, 7, -1  ] 
82:  [ JMPIE, 4, 7, -1  ] 
83:  [ ADDI, 7, -1, 41  ] 
84:  [ LDI, 1, -1, 0  ] 
85:  [ STD, 1, -1, 41  ] 
86:  [ SUBI, 3, -1, 1  ] 
87:  [ JMPIE, 4, 3, -1  ] 
88:  [ ADDI, 3, -1, 1  ] 
89:  [ LDI, 2, -1, 1  ] 
90:  [ STD, 2, -1, 42  ] 
91:  [ SUBI, 3, -1, 2  ] 
92:  [ JMPIE, 4, 3, -1  ] 
93:  [ LDI, 0, -1, 43  ] 
94:  [ LDI, 6, -1, 25  ] 
95:  [ LDI, 5, -1, 0  ] 
96:  [ ADD, 5, 7, -1  ] 
97:  [ LDI, 7, -1, 0  ] 
98:  [ ADD, 7, 5, -1  ] 
99:  [ LDI, 3, -1, 0  ] 
100:  [ ADD, 3, 1, -1  ] 
101:  [ LDI, 1, -1, 0  ] 
102:  [ ADD, 1, 2, -1  ] 
103:  [ ADD, 2, 3, -1  ] 
104:  [ STX, 0, 2, -1  ] 
105:  [ ADDI, 0, -1, 1  ] 
106:  [ SUB, 7, 0, -1  ] 
107:  [ JMPIG, 6, 7, -1  ] 
108:  [ STOP, -1, -1, -1  ] 
109:  [ DATA, -1, -1, -1  ] 
110:  [ DATA, -1, -1, -1  ] 
111:  [ DATA, -1, -1, -1  ] 
112:  [ DATA, -1, -1, -1  ] 
113:  [ DATA, -1, -1, 0  ] 
114:  [ DATA, -1, -1, 1  ] 
115:  [ DATA, -1, -1, 1  ] 
116:  [ DATA, -1, -1, 2  ] 
117:  [ DATA, -1, -1, 3  ] 
118:  [ DATA, -1, -1, 5  ] 
119:  [ DATA, -1, -1, 8  ] 
120:  [ DATA, -1, -1, 13  ] 
121:  [ DATA, -1, -1, 21  ] 
122:  [ DATA, -1, -1, 34  ] 
123:  [ DATA, -1, -1, 55  ] 
124:  [ DATA, -1, -1, -1  ] 
125:  [ DATA, -1, -1, -1  ] 
126:  [ DATA, -1, -1, -1  ] 
127:  [ DATA, -1, -1, -1  ] 
128:  [ DATA, -1, -1, 11  ] 
129:  [ ___, -1, -1, -1  ] 
130:  [ ___, -1, -1, -1  ] 
131:  [ ___, -1, -1, -1  ] 
132:  [ ___, -1, -1, -1  ] 
133:  [ ___, -1, -1, -1  ] 
134:  [ ___, -1, -1, -1  ] 
135:  [ ___, -1, -1, -1  ] 
8                                              //AQUI RESPONDENDO 2 CHAMADA DE SISTEMA
Requisitando resposta...
Instrucao OUT do processo 15 : 
[ TRAP, -1, -1, -1  ] 
Output: 5040
Selecione o comando que deseja:
[1] Cria processo
[2] Cria multiplos processos
[3] Dump
[4] DumpM
[5] Lista processos
[6] Liga/Desliga dump de resultado
                                               Interrupcao intIO   pc: 0
[7] Trace ON/Trace OFF
[8] Responder Pedido IO
[0] Exit
                                               Interrupcao intSTOP   pc: 17   id: 15

Id do processo = 15
Estado do processo = finalizado
Pagina do processo = 2
Pagina do processo = 3
Pagina do processo = 4
Conteudo do processo:
16:  [ LDI, 0, -1, 7  ] 
17:  [ STD, 0, -1, 19  ] 
18:  [ LDD, 0, -1, 19  ] 
19:  [ LDI, 1, -1, -1  ] 
20:  [ LDI, 2, -1, 13  ] 
21:  [ JMPIL, 2, 0, -1  ] 
22:  [ LDI, 1, -1, 1  ] 
23:  [ LDI, 6, -1, 1  ] 
24:  [ LDI, 7, -1, 13  ] 
25:  [ JMPIE, 7, 0, 0  ] 
26:  [ MULT, 1, 0, -1  ] 
27:  [ SUB, 0, 6, -1  ] 
28:  [ JMP, -1, -1, 9  ] 
29:  [ STD, 1, -1, 18  ] 
30:  [ LDI, 8, -1, 2  ] 
31:  [ LDI, 9, -1, 18  ] 
32:  [ TRAP, -1, -1, -1  ] 
33:  [ STOP, -1, -1, -1  ] 
34:  [ DATA, -1, -1, 5040  ] 
35:  [ DATA, -1, -1, 7  ] 
36:  [ ___, -1, -1, -1  ] 
37:  [ ___, -1, -1, -1  ] 
38:  [ ___, -1, -1, -1  ] 
39:  [ ___, -1, -1, -1  ] 
