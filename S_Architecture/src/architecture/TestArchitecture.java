package architecture;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import components.Memory;

public class TestArchitecture {
	
	//uncomment the anotation below to run the architecture showing components status
	//@Test
	public void testShowComponentes() {

		//a complete test (for visual purposes only).
		//a single code as follows
//		ldi 2
//		store 40
//		ldi -4
//		point:
//		store 41  //mem[41]=-4 (then -3, -2, -1, 0)
//		read 40
//		add 40    //mem[40] + mem[40]
//		store 40  //result must be in 40
//		read 41
//		inc
//		jn point
//		end
		
		Architecture arch = new Architecture(true);
		arch.getMemory().getDataList()[0]=7;
		arch.getMemory().getDataList()[1]=2;
		arch.getMemory().getDataList()[2]=6;
		arch.getMemory().getDataList()[3]=40;
		arch.getMemory().getDataList()[4]=7;
		arch.getMemory().getDataList()[5]=-4;
		arch.getMemory().getDataList()[6]=6;
		arch.getMemory().getDataList()[7]=41;
		arch.getMemory().getDataList()[8]=5;
		arch.getMemory().getDataList()[9]=40;
		arch.getMemory().getDataList()[10]=0;
		arch.getMemory().getDataList()[11]=40;
		arch.getMemory().getDataList()[12]=6;
		arch.getMemory().getDataList()[13]=40;
		arch.getMemory().getDataList()[14]=5;
		arch.getMemory().getDataList()[15]=41;
		arch.getMemory().getDataList()[16]=8;
		arch.getMemory().getDataList()[17]=4;
		arch.getMemory().getDataList()[18]=6;
		arch.getMemory().getDataList()[19]=-1;
		arch.getMemory().getDataList()[40]=0;
		arch.getMemory().getDataList()[41]=0;
		//now the program and the variables are stored. we can run
		arch.controlUnitEexec();
		
	}

	@Test
	public void testAdd() {
		Architecture arch = new Architecture();
		//storing the number 5 in position 40
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		//moreover, the number 40 must be in the position next to PC. (to perform add 40)
		//in this test, PC will point to 10 and the 40 will be in the position 11
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(40); //40 is in position 11
		arch.getMemory().store();
		arch.getExtbus1().put(10);
		arch.getPC().store();      //PC points to position 10
		
		//storing the number 8 in the RPG
		arch.getExtbus1().put(8);
		arch.getRPG().store();
		//now we can perform the add method. 
		//we will add the number 5 (stored in the 40th position in the memory) 
		//with the number 8 (already stored in the rgp)
		//result must be into rpg
		//pc must be two positions ahead the original one
		arch.add();
		arch.getRPG().read();
		//the bus must contains the number 13
		assertEquals(13, arch.getExtbus1().get());
		//the flags bits 0 and 1 must be 0
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(0, arch.getFlags().getBit(1));
		//PC must be pointing to 12
		arch.getPC().read();
		assertEquals(12, arch.getExtbus1().get());

	}
	
	@Test
	public void testAddMemReg() {
		Architecture arch = new Architecture();
		
		// mem[40] = 5 (o dado que será somado)
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// a instrução add <mem> %regA fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 40 (endereço do dado)
		// posição 12 -> 1  (id do regA = RPG1, índice 1 na lista de registradores)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(40); // 40 está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// RPG1 (regA) = 8: é ele quem deve receber o resultado, então NÃO pode
		// ser sobrescrito pelo valor da memória. O RPG é usado apenas como
		// ponte temporária do dado vindo da memória (RPG.store() no microprograma).
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.addMemReg();
		
		// RegA <- memória[40] + RegA  =>  5 + 8 = 13
		arch.getRegistersList().get(1).read();
		assertEquals(13, arch.getExtbus1().get());
		
		// o RPG termina com o valor da memória, pois foi a ponte do dado (5)
		arch.getRPG().read();
		assertEquals(5, arch.getExtbus1().get());
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testAddRegMem() {
		Architecture arch = new Architecture();
		
		// regA = RPG1 (índice 1) com o valor 8: é o primeiro operando
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		// mem[40] = 5 (segundo operando, vem da memória)
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// a instrução add %regA <mem> fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 1  (id do regA = RPG1)
		// posição 12 -> 40 (endereço do dado)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(40); // 40 (endereço) está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		arch.addRegMem();
		
		// Memória[40] <- RegA + memória[40]  =>  8 + 5 = 13
		arch.getExtbus1().put(40);
		arch.getMemory().read();
		assertEquals(13, arch.getExtbus1().get());
		
		// o RPG só foi a ponte do dado da memória e termina com 5
		arch.getRPG().read();
		assertEquals(13, arch.getExtbus1().get());
		
		// o regA (RPG1) não pode ser alterado: continua 8
		arch.getRegistersList().get(1).read();
		assertEquals(8, arch.getExtbus1().get());
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testSub() {
		Architecture arch = new Architecture();
		
		/*************************
		 * first test: 5 (rpg) - 8 (mem-40) = -3 (rpg)
		 ***********************************************/
		
		//storing the number 8 i the memory, in position 40
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(8);
		arch.getMemory().store();
		//storing the number 5 in the external bus
		arch.getExtbus1().put(5);
		//moving this number 5 into the rpg
		arch.getRPG().store();
		
		//moreover, the number 40 must be in the position next to PC. (to perform sub 40)
		//in this test, PC will point to 10 and the 40 will be in the position 11
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(40); //40 is in position 11
		arch.getMemory().store();
		arch.getExtbus1().put(10);
		arch.getPC().store();      //PC points to position 10
		
		//now we can perform the sub method. 
		//we will sub, from the number 5 (stored in the rpg) 
		//the number 8 (stored in the memory, position 40)
		//result must be into rpg
		arch.sub();
		arch.getRPG().read();
		//the bus must contains the number -3
		assertEquals(-3, arch.getExtbus1().get());
		
		//flags bits must be 0 (bit zero) and 1 (bit negative)
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(1, arch.getFlags().getBit(1));
		
		
		//PC must be pointing to 12
		arch.getPC().read();
		assertEquals(12, arch.getExtbus1().get());

		/*************************
		 * second test: 5 (rpg) - 5 (mem-50) = 0 (rpg)
		 ***********************************************/
		
		//storing the number 5 in the memory, in position 50
		arch.getExtbus1().put(50);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		//storing the number 5 in the external bus
		arch.getExtbus1().put(5);
		//moving this number 5 into the rpg
		arch.getRPG().store();
		
		//moreover, the number 50 must be in the position next to PC. (to perform sub 50)
		//in this test, PC will point to 12 and the 50 will be in the position 13
		arch.getExtbus1().put(13);
		arch.getMemory().store();
		arch.getExtbus1().put(50); //50 is in position 13
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getPC().store();      //PC points to position 12
		
		
		//now we can perform the sub method. 
		//we will sub, from the number 5 (stored in the rpg) 
		//the number 5 (already stored in the memory, position 50)
		//result must be into rpg
		arch.sub();
		arch.getRPG().read();
		//the bus must contains the number 0
		assertEquals(0, arch.getExtbus1().get());
		
		//flags bits must be 1 (bit zero) and 0 (bit negative)
		assertEquals(1, arch.getFlags().getBit(0));
		assertEquals(0, arch.getFlags().getBit(1));
		
		//PC must be pointing to 14
		arch.getPC().read();
		assertEquals(14, arch.getExtbus1().get());
		
		/*************************
		 * third test: 5 (rpg) - 3 (mem-60) = 2 (rpg)
		 ***********************************************/
		
		//storing the number 3 in the memory, in position 60
		arch.getExtbus1().put(60);
		arch.getMemory().store();
		arch.getExtbus1().put(3);
		arch.getMemory().store();
		//storing the number 5 in the external bus
		arch.getExtbus1().put(5);
		//moving this number 5 into the rpg
		arch.getRPG().store();
		
		//moreover, the number 60 must be in the position next to PC. (to perform sub 60)
		//in this test, PC will point to 14 and the 60 will be in the position 15
		arch.getExtbus1().put(15);
		arch.getMemory().store();
		arch.getExtbus1().put(60); //60 is in position 15
		arch.getMemory().store();
		arch.getExtbus1().put(14);
		arch.getPC().store();      //PC points to position 14
		
		
		//now we can perform the sub method. 
		//we will sub, from the number 5 (stored in the rpg) 
		//the number 3 (already stored in the memory, position 60)
		//result must be into rpg
		arch.sub();
		arch.getRPG().read();
		//the bus must contains the number 2
		assertEquals(2, arch.getExtbus1().get());
		
		//flags bits must be 0 (bit zero) and 0 (bit negative)
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(0, arch.getFlags().getBit(1));
		
		//PC must be pointing to 16
		arch.getPC().read();
		assertEquals(16, arch.getExtbus1().get());
	}
	
	@Test
	public void testSubRegReg() {
		Architecture arch = new Architecture();

		//o comando sub <regA> <regB> fica na posição 30, com dois parâmetros:
		//31 -> regA (primeiro operando)
		//32 -> regB (recebe o resultado)
		arch.getExtbus1().put(30);
		arch.getPC().store();          //PC points to position 30

		arch.getMemory().getDataList()[31] = 0; //regA = RPG0 (index 0)
		arch.getMemory().getDataList()[32] = 1; //regB = RPG1 (index 1)

		//valores: RPG0 = 5 (regA) e RPG1 = 8 (regB)
		arch.getExtbus1().put(5);
		arch.getRegistersList().get(0).store();
		
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();

		//executando: sub %RPG0 %RPG1  ->  RPG1 <- RPG0 - RPG1
		arch.subRegReg();

		//o resultado fica em regB (RPG1): 5 - 8 = -3
		arch.getRegistersList().get(1).read();
		assertEquals(-3, arch.getExtbus1().get());

		//regA (RPG0) deve ficar igual
		arch.getRegistersList().get(0).read();
		assertEquals(5, arch.getExtbus1().get());

		//resultado negativo -> bit 1 (negativo) ligado, bit 0 (zero) desligado
		assertEquals(1, arch.getFlags().getBit(1));
		assertEquals(0, arch.getFlags().getBit(0));

		//PC deve apontar 3 posições depois da inicial: 30 -> 33
		arch.getPC().read();
		assertEquals(33, arch.getExtbus1().get());
	}
	
	@Test
	public void testSubMemReg() {
		Architecture arch = new Architecture();
		
		// mem[40] = 5 (o minuendo vem da memória)
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// a instrução sub <mem> %regA fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 40 (endereço do dado)
		// posição 12 -> 1  (id do regA = RPG1, índice 1)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(40); // 40 está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8: é ele quem recebe o resultado
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.subMemReg();
		
		// RegA <- memória[40] - RegA  =>  5 - 8 = -3
		arch.getRegistersList().get(1).read();
		assertEquals(-3, arch.getExtbus1().get());
		
		// o RPG foi apenas a ponte do dado da memória: termina com 5
		arch.getRPG().read();
		assertEquals(5, arch.getExtbus1().get());
		
		// resultado negativo -> bit 1 ligado, bit 0 desligado
		assertEquals(1, arch.getFlags().getBit(1));
		assertEquals(0, arch.getFlags().getBit(0));
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testSubImmReg() {
		Architecture arch = new Architecture();
		
		// a instrução sub imm %regA fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 10 (valor imediato, lido direto da memória)
		// posição 12 -> 1  (id do regA = RPG1, índice 1)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(10); // o imediato 10 está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8: é ele quem recebe o resultado
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.subImmReg();
		
		// RegA <- imm - RegA  =>  10 - 8 = 2
		arch.getRegistersList().get(1).read();
		assertEquals(2, arch.getExtbus1().get());
		
		// o RPG foi apenas a ponte do imediato: termina com 10
		arch.getRPG().read();
		assertEquals(10, arch.getExtbus1().get());
		
		// resultado positivo e diferente de zero -> bits 0 e 1 desligados
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(0, arch.getFlags().getBit(1));
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testJmp() {
		Architecture arch = new Architecture();
		//storing the number 10 in PC
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();

		//storing the number 25 in the memory, in the position just before that one adressed by PC
		arch.getExtbus1().put(11); //the position is 11, once PC points to 10
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		
		
		//testing if PC stores the number 10
		arch.getPC().read();
		assertEquals(10, arch.getExtbus1().get());
		
		//now we can perform the jmp method. 
		//we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC
		arch.jmp();
		arch.getPC().internalRead();;
		//the internalbus2 must contains the number 25
		assertEquals(25, arch.getIntbus1().get());

	}
	
	@Test
	public void testJz() {
		Architecture arch = new Architecture();
		
		//storing the number 30 in PC
		arch.getIntbus1().put(30);
		arch.getPC().internalStore();
		
		//storing the number 25 in the into the memory, in position 31, the position just after PC
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();

		//now we can perform the jz method. 

		//CASE 1.
		//Bit ZERO is equals to 1
		arch.getFlags().setBit(0, 1);
		
		//So, we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());		

		arch.jz();
		
		//PC must be storing the number 25
		arch.getPC().internalRead();
		assertEquals(25, arch.getIntbus1().get());
		
		//CASE 2.
		//Bit ZERO is equals to 0
		arch.getFlags().setBit(0, 0);
		//PC must have the number 30 initially (in this time, by using the external bus)
		arch.getExtbus1().put(30);
		arch.getPC().store();
		//destroying the data in external bus
		arch.getExtbus1().put(0);

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());	
		
		//Note that the memory was not changed. So, in position 31 we have the number 25
		
		//Once the ZERO bit is 0, we WILL NOT move the number 25 (stored in the 31th position in the memory)
		//into the PC.
		//The original PC position was 30. The parameter is in position 31. So, now PC must be pointing to 32
		arch.jz();
		//PC contains the number 32
		arch.getPC().internalRead();
		assertEquals(32, arch.getIntbus1().get());
	}
	
	@Test
	public void testJn() {
		Architecture arch = new Architecture();
		
		//storing the number 30 in PC
		arch.getIntbus1().put(30);
		arch.getPC().internalStore();
		
		//storing the number 25 in the into the memory, in position 31, the position just after PC
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();

		//now we can perform the jn method. 

		//CASE 1.
		//Bit NEGATIVE is equals to 1
		arch.getFlags().setBit(1, 1);
		
		//So, we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());		

		arch.jn();
		
		//PC must be storng the number 25
		arch.getPC().internalRead();
		assertEquals(25, arch.getIntbus1().get());
		
		//CASE 2.
		//Bit NEGATIVE is equals to 0
		arch.getFlags().setBit(1, 0);
		//PC must have the number 30 initially (in this time, by using the external bus)
		arch.getExtbus1().put(30);
		arch.getPC().store();
		//destroying the data in external bus
		arch.getExtbus1().put(0);

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());	
		
		//Note that the memory was not changed. So, in position 31 we have the number 25
		
		//Once the ZERO bit is 0, we WILL NOT move the number 25 (stored in the 31th position in the memory)
		//into the PC.
		//The original PC position was 30. The parameter is in position 31. So, now PC must be pointing to 32
		arch.jn();
		//PC contains the number 32
		arch.getPC().internalRead();
		assertEquals(32, arch.getIntbus1().get());
	}
	
	@Test
	public void testJmpMem() {
		Architecture arch = new Architecture();
		
		// PC = 10
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		// mem[11] = 25 (o alvo do desvio, parâmetro do comando)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		
		// PC deve estar em 10 antes do desvio
		arch.getPC().read();
		assertEquals(10, arch.getExtbus1().get());
		
		arch.jmpMem();
		
		// PC <- mem[11] = 25 (desvio incondicional)
		arch.getPC().internalRead();
		assertEquals(25, arch.getIntbus1().get());
	}
	
	@Test
	public void testJnz() {
		Architecture arch = new Architecture();
		
		// PC = 30; mem[31] = 25 (alvo do desvio)
		arch.getIntbus1().put(30);
		arch.getPC().internalStore();
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		
		// CASE 1.
		// Bit ZERO = 0 (resultado diferente de zero) -> DESVIA
		arch.getFlags().setBit(0, 0);
		
		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());
		
		arch.jnz();
		
		//PC must be storing the number 25 (alvo)
		arch.getPC().internalRead();
		assertEquals(25, arch.getIntbus1().get());
		
		// CASE 2.
		// Bit ZERO = 1 (resultado zero) -> NÃO desvia
		arch.getFlags().setBit(0, 1);
		//restaurando PC = 30 (o parâmetro em mem[31] não mudou)
		arch.getExtbus1().put(30);
		arch.getPC().store();
		//destroying the data in external bus
		arch.getExtbus1().put(0);
		
		arch.jnz();
		
		//PC = 32 (30 + opcode + 1 parâmetro): desvio não tomado
		arch.getPC().internalRead();
		assertEquals(32, arch.getIntbus1().get());
	}
	
	@Test
	public void testJeq() {
		Architecture arch = new Architecture();
		
		// o comando jeq %regA %regB <mem> fica na posição 10 e tem TRÊS parâmetros:
		// 11 -> 0 (id do regA = RPG, índice 0)
		// 12 -> 1 (id do regB = RPG1, índice 1)
		// 13 -> 50 (alvo do desvio)
		arch.getMemory().getDataList()[11] = 0;
		arch.getMemory().getDataList()[12] = 1;
		arch.getMemory().getDataList()[13] = 50;
		
		// CASE 1: RegA == RegB -> DESVIA
		arch.getExtbus1().put(7);
		arch.getRegistersList().get(0).store(); // regA = 7
		arch.getExtbus1().put(7);
		arch.getRegistersList().get(1).store(); // regB = 7
		
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		arch.jeq();
		
		// 7 - 7 = 0 -> bit ZERO ligado (os flags vêm do RESULTADO da subtração)
		assertEquals(1, arch.getFlags().getBit(0));
		
		// PC <- 50 (desvio tomado)
		arch.getPC().internalRead();
		assertEquals(50, arch.getIntbus1().get());
		
		// CASE 2: RegA != RegB -> NÃO desvia
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store(); // regB = 8 (regA continua 7)
		
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		arch.jeq();
		
		// 7 - 8 = -1 -> bit ZERO desligado, bit NEG ligado
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(1, arch.getFlags().getBit(1));
		
		// PC = 14 (10 + opcode + 3 parâmetros): desvio NÃO tomado
		arch.getPC().internalRead();
		assertEquals(14, arch.getIntbus1().get());
	}
	
	@Test
	public void testJlw() {
		Architecture arch = new Architecture();
		
		// o comando jlw %regA %regB <mem> fica na posição 10 e tem TRÊS parâmetros:
		// 11 -> 0 (id do regA = RPG, índice 0)
		// 12 -> 1 (id do regB = RPG1, índice 1)
		// 13 -> 50 (alvo do desvio)
		arch.getMemory().getDataList()[11] = 0;
		arch.getMemory().getDataList()[12] = 1;
		arch.getMemory().getDataList()[13] = 50;
		
		// CASE 1: RegA(5) < RegB(8) -> DESVIA
		arch.getExtbus1().put(5);
		arch.getRegistersList().get(0).store(); // regA = 5
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store(); // regB = 8
		
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		arch.jlw();
		
		// 5 - 8 = -3 -> bit NEG ligado, bit ZERO desligado
		// (os flags vêm do RESULTADO da subtração)
		assertEquals(1, arch.getFlags().getBit(1));
		assertEquals(0, arch.getFlags().getBit(0));
		
		// PC <- 50 (desvio tomado)
		arch.getPC().internalRead();
		assertEquals(50, arch.getIntbus1().get());
		
		// CASE 2: RegA(5) == RegB(5) -> NÃO desvia (menor ESTRICTO)
		arch.getExtbus1().put(5);
		arch.getRegistersList().get(1).store(); // regB = 5 (regA continua 5)
		
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		arch.jlw();
		
		// 5 - 5 = 0 -> bit NEG desligado
		assertEquals(0, arch.getFlags().getBit(1));
		// PC = 14 (10 + opcode + 3 parâmetros): desvio NÃO tomado
		arch.getPC().internalRead();
		assertEquals(14, arch.getIntbus1().get());
		
		// CASE 3: RegA(8) > RegB(5) -> NÃO desvia
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(0).store(); // regA = 8 (regB continua 5)
		
		arch.getIntbus1().put(10);
		arch.getPC().internalStore();
		
		arch.jlw();
		
		// 8 - 5 = 3 -> bit NEG desligado
		assertEquals(0, arch.getFlags().getBit(1));
		// PC = 14: desvio NÃO tomado
		arch.getPC().internalRead();
		assertEquals(14, arch.getIntbus1().get());
	}
	
	@Test
	public void testRead() {
		Architecture arch = new Architecture();
		//storing the number 10 in RPG
		arch.getIntbus1().put(10);
		arch.getRPG().internalStore();
		//testing if RPG stores the number 10
		arch.getRPG().read();
		assertEquals(10, arch.getExtbus1().get());
		
		//storing the number 25 in the memory, in position 31
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		
		//storing the number -100 in the memory, in position 25
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		arch.getExtbus1().put(-100);
		arch.getMemory().store();
		
		//PC must be pointing to the address just before the parameter (where is the instruction)
		arch.getExtbus1().put(30);
		arch.getPC().store();

		//now we can perform the read method. 
		//scenery PC=30, mem[31]=25, mem[25]=-100 RPG=10
		//we will move the the number 25 (stored in the 31th position in the memory) 
		//into the RPG by using the move command (move 31)

		arch.read();
		arch.getRPG().internalRead();
		//the internalbus1 must contain the number -100 (that is stored in position 25)
		assertEquals(-100, arch.getRPG().getData());
		
		//PC must be pointing two positions after its original value
		arch.getPC().read();
		assertEquals(32, arch.getExtbus1().get());

	}
	
	@Test
	public void testStore() {
		Architecture arch = new Architecture();

		//storing the number 25 in the memory, in position 31
		arch.getMemory().getDataList()[31]=25;
		

		//now we can perform the store method. 
		//store X stores, in the position X, the data that is currently in RPG
		
		//let's put PC pointing to the position 11
		arch.getExtbus1().put(11);
		arch.getPC().store();
		//now lets put the parameter (the position where the data will be stored) into the position next to PC
		arch.getMemory().getDataList()[12]=31;
		
		//storing the number 155 in RPG
		arch.getIntbus1().put(155);
		arch.getRPG().internalStore();
		
		//testing if memory contains the number 25 in the 31th position
		arch.getExtbus1().put(31);
		arch.getMemory().read();
		assertEquals(25, arch.getExtbus1().get());
		
		//So, PC is pointing to memory[11], memory[12] has 31, memory [31] has 25 and RPG has 155
		
		//destroying data in externalbus 1
		arch.getExtbus1().put(0);

		arch.store();

		//now, memory[31] must be updated from 25 to 155
		assertEquals(155, arch.getMemory().getDataList()[31]);
		
		//PC must be pointing two positions after its original value
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());


	}
	
	@Test
	public void testLdi() {
		Architecture arch = new Architecture();
		//storing the number 10 in RPG
		arch.getIntbus1().put(10);
		arch.getRPG().internalStore();
		
		//the scenery: PC points to 50, mem[51] (parameter) is -40
		
		arch.getExtbus1().put(51);
		arch.getMemory().store();
		arch.getExtbus1().put(-40);
		arch.getMemory().store();
		
		arch.getExtbus1().put(50);
		arch.getPC().store();
		
		//destroying data in internalbus 1
		arch.getIntbus1().put(0);

		//now we can perform the ldi method. 
		//we will move the the number -40 (immediate value) 
		//into the rpg
		arch.ldi();
		
		arch.getRPG().read();
		//the externalbus1 must contains the number 44
		assertEquals(-40, arch.getExtbus1().get());
		
		//PC must be pointing two positions after its original value
		arch.getPC().read();
		assertEquals(52, arch.getExtbus1().get());

	}
	
	@Test
	public void testInc() {
		Architecture arch = new Architecture();
		//storing the number 10 in RPG
		arch.getExtbus1().put(10);
		arch.getRPG().store();
		//testing if RPG stores the number 10
		arch.getRPG().read();
		assertEquals(10, arch.getExtbus1().get());

		//destroying data in externalbus 1
		arch.getExtbus1().put(0);
		
		//pc points to 50 (where we suppose the instruction is
		arch.getExtbus1().put(50);
		arch.getPC().store();

		//now we can perform the inc method. 
		arch.inc();
		arch.getRPG().read();
		//the externalbus1 must contains the number 11
		assertEquals(11, arch.getExtbus1().get());
		
		//PC must be pointing ONE position after its original value, because this command has no parameters!
		arch.getPC().read();
		assertEquals(51, arch.getExtbus1().get());

	}
	
	@Test
	public void testIncReg() {
		Architecture arch = new Architecture();
		
		// a instrução inc %regA fica na posição 10 e tem UM parâmetro:
		// posição 11 -> 1 (id do regA = RPG1, índice 1)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 11
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.incReg();
		
		// RegA++  =>  8 + 1 = 9
		arch.getRegistersList().get(1).read();
		assertEquals(9, arch.getExtbus1().get());
		
		// flags: 9 é positivo e diferente de zero -> bits 0 e 1 desligados
		assertEquals(0, arch.getFlags().getBit(0));
		assertEquals(0, arch.getFlags().getBit(1));
		
		// PC avança 2 posições: 10 -> 12 (opcode + 1 parâmetro)
		arch.getPC().read();
		assertEquals(12, arch.getExtbus1().get());
	}
	
	@Test
	public void testMoveRegReg() {
		Architecture arch = new Architecture();

		//a instrução move REG1 REG0 fica na posição 30 e tem DOIS parâmetros:
		//posição 31 -> 1 (id do REG1 = RPG1, índice 1) = fonte
		//posição 32 -> 0 (id do REG0 = RPG,  índice 0) = destino
		arch.getMemory().getDataList()[31]=1;
		arch.getMemory().getDataList()[32]=0;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //REG0 (destino) tem 45
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(1).store(); //REG1 (fonte) tem 99
		
		//executing the command move REG1 REG0.
		arch.moveRegReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(99, arch.getExtbus1().get()); //destino recebeu o valor da fonte
		arch.getRegistersList().get(1).read();
		assertEquals(99, arch.getExtbus1().get()); //fonte permanece igual
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());
	}
	
	@Test
	public void testMoveRegMem() {
		Architecture arch = new Architecture();
		
		// mem[40] = 5 (o dado que será movido)
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// a instrução move <mem> %regA fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 40 (endereço do dado)
		// posição 12 -> 1  (id do regA = RPG1, índice 1)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(40); // 40 está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8: valor antigo, deve ser substituído pelo dado
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.moveRegMem();
		
		// RegA <- memória[40]  =>  RPG1 = 5
		arch.getRegistersList().get(1).read();
		assertEquals(5, arch.getExtbus1().get());
		
		// o RPG foi apenas a ponte do dado da memória: termina com 5
		arch.getRPG().read();
		assertEquals(5, arch.getExtbus1().get());
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testMoveMemReg() {
		Architecture arch = new Architecture();
		
		// mem[40] = 7 (valor antigo, deve ser substituído)
		arch.getExtbus1().put(40);
		arch.getMemory().store();
		arch.getExtbus1().put(7);
		arch.getMemory().store();
		
		// a instrução move %regA <mem> fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 1  (id do regA = RPG1, índice 1)
		// posição 12 -> 40 (endereço de destino)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(40); // 40 (endereço) está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8: é a fonte do dado
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.moveMemReg();
		
		// memória[40] <- RegA  =>  mem[40] = 8
		arch.getExtbus1().put(40);
		arch.getMemory().read();
		assertEquals(8, arch.getExtbus1().get());
		
		// o regA fonte permanece igual: 8
		arch.getRegistersList().get(1).read();
		assertEquals(8, arch.getExtbus1().get());
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testMoveImmReg() {
		Architecture arch = new Architecture();
		
		// a instrução move imm %regA fica na posição 10 e tem DOIS parâmetros:
		// posição 11 -> 42 (valor imediato, lido direto da memória)
		// posição 12 -> 1  (id do regA = RPG1, índice 1)
		arch.getExtbus1().put(11);
		arch.getMemory().store();
		arch.getExtbus1().put(42); // o imediato 42 está na posição 11
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // o id do regA está na posição 12
		arch.getMemory().store();
		
		arch.getExtbus1().put(10);
		arch.getPC().store(); // PC aponta para posição 10
		
		// regA (RPG1) = 8: valor antigo, deve ser substituído pelo imediato
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(1).store();
		
		arch.moveImmReg();
		
		// RegA <- imm  =>  RPG1 = 42
		arch.getRegistersList().get(1).read();
		assertEquals(42, arch.getExtbus1().get());
		
		// o RPG foi apenas a ponte do imediato: termina com 42
		arch.getRPG().read();
		assertEquals(42, arch.getExtbus1().get());
		
		// PC avança 3 posições: 10 -> 13 (opcode + 2 parâmetros)
		arch.getPC().read();
		assertEquals(13, arch.getExtbus1().get());
	}
	
	@Test
	public void testAddRegReg() {
		Architecture arch = new Architecture();

		//o comando (addRegReg = 10) é guardado na posição 30.
		//sao dois parâmetros nas duas próximas posições:
		//31 ->  regA (the first operand)
		//32 ->  regB (o segundo operando vai receber o resultado)
		arch.getExtbus1().put(30);
		arch.getPC().store();          //PC points to position 30

		// guardando o numero 0 na memoria, na posição 31
		arch.getMemory().getDataList()[31] = 0; //regA = RPG0 (index 0)
		// guardando o numero 1 na memoria, na posição 32
		arch.getMemory().getDataList()[32] = 1; //regB = RPG1 (index 1)

		//agora colocando os valores nos registradores: RPG0 = 8 e RPG1 = 5
		arch.getExtbus1().put(8);
		arch.getRegistersList().get(0).store();
		
		arch.getExtbus1().put(5);
		arch.getRegistersList().get(1).store();

		//executando: add %RPG0 %RPG1   ->   RPG1 <- RPG0 + RPG1
		arch.addRegReg();

		//o resultado deve está em regB (RPG1): 8 + 5 = 13
		arch.getRegistersList().get(1).read();
		assertEquals(13, arch.getExtbus1().get());

		//regA (RPG0) deve ficar igual
		arch.getRegistersList().get(0).read();
		assertEquals(8, arch.getExtbus1().get());

		//PC deve apontar 3 posições depois da inicial: 30 -> 33
		arch.getPC().read();
		assertEquals(33, arch.getExtbus1().get());
	
	}
	
	@Test
	public void testFillCommandsList() {
		
		//all the instructions must be in Commands List
		/*
		 *
				add addr (rpg <- rpg + addr)
				sub addr (rpg <- rpg - addr)
				jmp addr (pc <- addr)
				jz addr  (se bitZero pc <- addr)
				jn addr  (se bitneg pc <- addr)
				read addr (rpg <- addr)
				store addr  (addr <- rpg)
				ldi x    (rpg <- x. x must be an integer)
				inc    (rpg++)
				move %reg0 %reg1 (reg1 <- Reg0)
		 */

		
		
		Architecture arch = new Architecture();
		ArrayList<String> commands = arch.getCommandsList();
		assertTrue("add".equals(commands.get(0)));
		assertTrue("sub".equals(commands.get(1)));
		assertTrue("jmp".equals(commands.get(2)));
		assertTrue("jz".equals(commands.get(3)));
		assertTrue("jn".equals(commands.get(4)));
		assertTrue("read".equals(commands.get(5)));
		assertTrue("store".equals(commands.get(6)));
		assertTrue("ldi".equals(commands.get(7)));
		assertTrue("inc".equals(commands.get(8)));
		assertTrue("moveRegReg".equals(commands.get(9)));
	}
	
	@Test
	public void testReadExec() throws IOException {
		Architecture arch = new Architecture();
		arch.readExec("testFile");
		assertEquals(5, arch.getMemory().getDataList()[0]);
		assertEquals(4, arch.getMemory().getDataList()[1]);
		assertEquals(3, arch.getMemory().getDataList()[2]);
		assertEquals(2, arch.getMemory().getDataList()[3]);
		assertEquals(1, arch.getMemory().getDataList()[4]);
		assertEquals(0, arch.getMemory().getDataList()[5]);
	}

}
