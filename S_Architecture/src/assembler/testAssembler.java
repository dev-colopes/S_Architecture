package assembler;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class testAssembler {

    @Test
    public void testProccessCommand() {
        String[][] cases = {
            {"add t", "0 &t"}, {"sub t", "1 &t"}, {"jmp fim", "2 &fim"},
            {"jz fim", "3 &fim"}, {"jn fim", "4 &fim"}, {"read t", "5 &t"},
            {"store t", "6 &t"}, {"ldi -3", "7 -3"}, {"inc", "8"},
            {"move %RPG0 %RPG1", "9 %RPG0 %RPG1"},
            {"add %RPG1 %RPG2", "10 %RPG1 %RPG2"}, {"add t %RPG2", "11 &t %RPG2"},
            {"add %RPG2 t", "12 %RPG2 &t"}, {"sub %RPG1 %RPG2", "13 %RPG1 %RPG2"},
            {"sub -5 %RPG1", "14 -5 %RPG1"}, {"jmpMem fim", "15 &fim"},
            {"jnz fim", "16 &fim"}, {"jeq %RPG0 %RPG3 fim", "17 %RPG0 %RPG3 &fim"},
            {"jlw %RPG1 %RPG2 fim", "18 %RPG1 %RPG2 &fim"}, {"inc %RPG3", "19 %RPG3"},
            {"move t %RPG2", "21 &t %RPG2"}, {"move %RPG2 t", "22 %RPG2 &t"},
            {"move 5 %RPG1", "23 5 %RPG1"}
        };
        for (String[] example : cases) {
            Assembler assembler = new Assembler();
            assembler.proccessCommand(example[0].split(" "));
            assertEquals(example[0], java.util.Arrays.asList(example[1].split(" ")),
                    assembler.getObjProgram());
        }
    }

	@Test
	public void testParse() {
		Assembler ass = new Assembler();
		ArrayList<String> returnedObj = new ArrayList<>();
		ArrayList<String> sourceProgram = new ArrayList<>();
		
		//inserting the following program
		/*
		 * var1
		 * var2
		 * var3
		 * ldi 10
		 * store var3
		 * ldi 2
		 * store var2
		 * ldi 0
		 * store var1
		 * label:
		 * read var1
		 * add var2
		 * store var1
		 * move %RPG1 %RPG0
		 * sub var3
		 * jn label
		 */
		sourceProgram.add("var1");
		sourceProgram.add("var2");
		sourceProgram.add("var3");
		sourceProgram.add("ldi 10");
		sourceProgram.add("store var3");
		sourceProgram.add("ldi 2");
		sourceProgram.add("store var2");
		sourceProgram.add("ldi 0");
		sourceProgram.add("store var1");
		sourceProgram.add("label:");
		sourceProgram.add("read var1");
		sourceProgram.add("add var2");
		sourceProgram.add("store var1");
		sourceProgram.add("move %RPG1 %RPG0");
		sourceProgram.add("sub var3");
		sourceProgram.add("jn label");
		
		//now we can generate the object program
		ass.setLines(sourceProgram);
		ass.parse();
		returnedObj = ass.getObjProgram();
		
		//testing
		assertEquals(25, returnedObj.size());
		
		//checking line by line
		assertEquals("7", returnedObj.get(0)); //the code of ldi is 7
		assertEquals("10", returnedObj.get(1)); //the parameter
		
		assertEquals("6", returnedObj.get(2)); //the code of store is 6
		assertEquals("&var3", returnedObj.get(3)); //the parameter
		
		assertEquals("7", returnedObj.get(4)); //the code of ldi is 7
		assertEquals("2", returnedObj.get(5)); //the parameter
		
		assertEquals("6", returnedObj.get(6)); //the code of store is 6
		assertEquals("&var2", returnedObj.get(7)); //the parameter
		
		assertEquals("7", returnedObj.get(8)); //the code of ldi is 7
		assertEquals("0", returnedObj.get(9)); //the parameter
		
		assertEquals("6", returnedObj.get(10)); //the code of store is 6
		assertEquals("&var1", returnedObj.get(11)); //the parameter
		
		assertEquals("5", returnedObj.get(12)); //the code of read is 5
		assertEquals("&var1", returnedObj.get(13)); //the parameter
		
		assertEquals("0", returnedObj.get(14)); //the code of add is 0
		assertEquals("&var2", returnedObj.get(15)); //the parameter
		
		assertEquals("6", returnedObj.get(16)); //the code of store is 6
		assertEquals("&var1", returnedObj.get(17)); //the parameter
		
		assertEquals("9", returnedObj.get(18)); //the code of moveRegReg is 9
		assertEquals("%RPG1", returnedObj.get(19)); //the parameter
		assertEquals("%RPG0", returnedObj.get(20)); //the parameter
		
		assertEquals("1", returnedObj.get(21)); //the code of sub is 1
		assertEquals("&var3", returnedObj.get(22)); //the parameter
		
		assertEquals("4", returnedObj.get(23)); //the code of jn is 4
		assertEquals("&label", returnedObj.get(24)); //the parameter
		
		//now, checking if the label "label" was inserted, pointing to the position 12
		//the line 'read var1' is just after the label
		//once the command was inserted in the position 12, the label must 
		//be pointing to the position 12
		assertTrue(ass.getLabels().contains("label"));
		assertEquals(1, ass.getLabels().size());
		assertEquals(1, ass.getLabelsAddresses().size());
		assertEquals(0, ass.getLabels().indexOf("label"));
		assertEquals(12, (int) ass.getLabelsAddresses().get(0));
		
		//checking if all variables are stored in variables collection
		assertEquals("var1", ass.getVariables().get(0));
		assertEquals("var2", ass.getVariables().get(1));
		assertEquals("var3", ass.getVariables().get(2));
	}

	@Test
	public void testReplaceVariable() {
		
		Assembler ass = new Assembler();
		ArrayList<String> sampleexec = new ArrayList<>();
		
		//creating a fictional exec program with some variables
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 1
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&var2"); //var2 in the position 5
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 7
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&var3"); //var3 in the position 10
		sampleexec.add("9");
		sampleexec.add("&var3"); //var3 in the position 12
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 14
		
		//inserting this arraylist into the execprogram collections
		ass.setExecProgram(sampleexec);
		
		//now the test!
		//var1 must be replaced by position 100
		//var2 must be replaced by  position 99
		//var2 must be replaced by  position 98
		
		ass.replaceVariable("var1", 100);
		ass.replaceVariable("var2", 99);
		ass.replaceVariable("var3", 98);
		
		//getting the positions 
		//var1 is now the address 100. It must be found in positions 1, 7 and 14 
		assertEquals("100", ass.getExecProgram().get(1));
		assertEquals("100", ass.getExecProgram().get(7));
		assertEquals("100", ass.getExecProgram().get(14));
		
		//var2 is now the address 99. It must be found in positions 5 
		assertEquals("99", ass.getExecProgram().get(5));
		
		//var3 is now the address 98. It must be found in positions 10 and 12
		assertEquals("98", ass.getExecProgram().get(10));
		assertEquals("98", ass.getExecProgram().get(12));
		
	}
	
	@Test
	public void testReplaceLabels() {
		Assembler ass = new Assembler();
		ArrayList<String> sampleexec = new ArrayList<>();
		
		//creating a fictional exec program with some labels
		sampleexec.add("9");
		sampleexec.add("&label1"); //label1 in the position 1
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&label2"); //label2 in the position 5
		sampleexec.add("9");
		sampleexec.add("&label1"); //label1 in the position 7
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&label3"); //label3 in the position 10
		sampleexec.add("9");
		sampleexec.add("&label3"); //label3 in the position 12
		sampleexec.add("9");
		sampleexec.add("&label1"); //label1 in the position 14

		//inserting the labels
		ass.getLabels().add("label1");
		ass.getLabels().add("label2");
		ass.getLabels().add("label3");

		//and inserting the label addresses
		ass.getLabelsAddresses().add(17); //label 1 means position 17
		ass.getLabelsAddresses().add(42); //label 1 means position 42
		ass.getLabelsAddresses().add(63); //label 1 means position 63
		
		
		//inserting this arraylist we made above into the execprogram collections
		ass.setExecProgram(sampleexec);
		
		
		//now the test!
		ass.replaceLabels();
		//label1 (now refering to position 17) in positions 10 and 12
		assertEquals("17", ass.getExecProgram().get(1));
		assertEquals("17", ass.getExecProgram().get(7));
		assertEquals("17", ass.getExecProgram().get(14));
		
		//label2 (now refering to position 42) in positions 5
		assertEquals("42", ass.getExecProgram().get(5));
		
		//label3 (now refering to position 63) in positions 1, 7 and 14
		assertEquals("63", ass.getExecProgram().get(10));
		assertEquals("63", ass.getExecProgram().get(12));
	}

	@Test
	public void testReplaceAllVariables() {
		Assembler ass = new Assembler();
		ArrayList<String> sampleexec = new ArrayList<>();
		
		//creating a fictional exec program with some variables
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 1
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&var2"); //var2 in the position 5
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 7
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("&var3"); //var3 in the position 10
		sampleexec.add("9");
		sampleexec.add("&var3"); //var3 in the position 12
		sampleexec.add("9");
		sampleexec.add("&var1"); //var1 in the position 14
		
		//inserting this arraylist into the execprogram collections
		ass.setExecProgram(sampleexec);
		
		//now inserting variables
		ass.getVariables().add("var1");
		ass.getVariables().add("var2");
		ass.getVariables().add("var3");
		
		//in this architecture, the memory size is 128
		//so, var1 must be replaced by 127, var2 by 126 and var3 by 125
		ass.replaceAllVariables();
		//var1 (now 127) is in lines 5, 7 and 14
		assertEquals("127", ass.getExecProgram().get(1));
		assertEquals("127", ass.getExecProgram().get(7));
		assertEquals("127", ass.getExecProgram().get(14));
		
		//var2 (now 126) is in line 5
		assertEquals("126", ass.getExecProgram().get(5));
		
		//var3 (now 125) is in lines 10 and 12
		assertEquals("125", ass.getExecProgram().get(10));
		assertEquals("125", ass.getExecProgram().get(12));
	}
	
	@Test
	public void testReplaceRegisters() {
		
		Assembler ass = new Assembler();
		ArrayList<String> sampleexec = new ArrayList<>();
		
		//creating a fictional exec program with some registers
		sampleexec.add("9");
		sampleexec.add("%RPG1"); //rpg1 in the position 1
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("%PC"); //pc is in position 4
		sampleexec.add("9");
		sampleexec.add("%RPG0"); //rpg0 is in position 6
		sampleexec.add("9");
		sampleexec.add("9");
		sampleexec.add("%IR"); //ir in the position 9
		sampleexec.add("9");
		sampleexec.add("%RPG0"); //rpg0 in the position 11
		sampleexec.add("9");
		sampleexec.add("%RPG1"); //rpg1 in the position 13
		
		//inserting this arraylist into the execprogram collections
		ass.setExecProgram(sampleexec);
		
		//now the test!
		//RPG0 must be replaced by 0 
		//RPG1 must be replaced by 1
		//PC must be replaced by 4
		//IR must be replaced by 5
		
		ass.replaceRegisters();
		
		//getting the positions 
		//rpg0 is now the number 0. It must be found in positions 6 and 11
		assertEquals("0", ass.getExecProgram().get(6));
		assertEquals("0", ass.getExecProgram().get(11));
		
		//rpg1 is now the number 1. It must be found in positions 1 and 13 
		assertEquals("1", ass.getExecProgram().get(1));
		assertEquals("1", ass.getExecProgram().get(13));
		
		//pc is now the number 4. It must be found in position 4 
		assertEquals("4", ass.getExecProgram().get(4));

		//ir is now the number 5. It must be found in position 9 
		assertEquals("5", ass.getExecProgram().get(9));
		

		
	}
	
	@Test
	public void testCheckLabels() {
		Assembler ass = new Assembler();
		ArrayList<String> sampleexec = new ArrayList<>();
		
		//creating a fictional exec program with some variables
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label1"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label2"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&var1"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label3"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label3"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&var1"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label1"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&var2"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&var1"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label3"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&var3"); 
		ass.getObjProgram().add("9");
		ass.getObjProgram().add("&label1"); 
		
		
		//case 1: some labels are not found
		
						//inserting the labels
						ass.getLabels().add("label1");
						ass.getLabels().add("label2");
						//label 3 is missing
						

								
						//now inserting variables
						ass.getVariables().add("var1");
						ass.getVariables().add("var2");
						ass.getVariables().add("var3");

		
						assertFalse(ass.checkLabels());

						
		//case 2: some variables are not found
						
						ass.getLabels().clear();
						ass.getVariables().clear();


						//inserting the labels
						ass.getLabels().add("label1");
						ass.getLabels().add("label2");
						ass.getLabels().add("label3");
				
								
						//now inserting variables
						ass.getVariables().add("var1");
						ass.getVariables().add("var2");
						//var3 is missing
		
						assertFalse(ass.checkLabels());
						
		//case 3: all variables and labels are found
						
						ass.getLabels().clear();
						ass.getVariables().clear();


						//inserting the labels
						ass.getLabels().add("label1");
						ass.getLabels().add("label2");
						ass.getLabels().add("label3");
				
								
						//now inserting variables
						ass.getVariables().add("var1");
						ass.getVariables().add("var2");
						ass.getVariables().add("var3");
		
						assertTrue(ass.checkLabels());
		
	}
	
	
	@Test
	public void testRead() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("assembler-test");
        java.nio.file.Path source = dir.resolve("program.dsf");
        java.nio.file.Path output = dir.resolve("program.dxf");
        try {
            java.nio.file.Files.write(source, java.util.Arrays.asList(
                    "t", "ldi 5", "store t", "inicio:", "add t", "jn inicio"));
            String base = dir.resolve("program").toString();
            Assembler assembler = new Assembler();
            assembler.read(base);
            assembler.parse();
            assembler.makeExecutable(base);
            assertEquals(java.util.Arrays.asList("7", "5", "6", "127", "0", "127", "4", "4", "-1"),
                    java.nio.file.Files.readAllLines(output));
        } finally {
            java.nio.file.Files.deleteIfExists(output);
            java.nio.file.Files.deleteIfExists(source);
            java.nio.file.Files.deleteIfExists(dir);
        }
    }
}