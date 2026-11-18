package assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import components.Register;

import architecture.Architecture;

public class Assembler {
	
	private ArrayList<String> lines;
	private ArrayList<String> objProgram;
	private ArrayList<String> execProgram;
	private Architecture arch;
	private ArrayList<String>commands;	
	private ArrayList<String>labels;
	private ArrayList<Integer> labelsAdresses;
	private ArrayList<String>variables;
	
	
	public Assembler() {
		lines = new ArrayList<>();
		labels = new ArrayList<>();
		labelsAdresses = new ArrayList<>();
		variables = new ArrayList<>();
		objProgram = new ArrayList<>();
		execProgram = new ArrayList<>();
		arch = new Architecture();
		commands = arch.getCommandsList();	
	}
	
	//getters
	
	public ArrayList<String> getObjProgram() {
		return objProgram;
	}
	
	/**
	 * These methods getters and set below are used only for TDD purposes
	 * @param lines
	 */
	
	protected ArrayList<String> getLabels() {
		return labels;
	}
	
	protected ArrayList<Integer> getLabelsAddresses() {
		return labelsAdresses;
	}
	
	protected ArrayList<String> getVariables() {
		return variables;
	}
	
	protected ArrayList<String> getExecProgram() {
		return execProgram;
	}
	
	protected void setLines(ArrayList<String> lines) {
		this.lines = lines;
	}	

	protected void setExecProgram(ArrayList<String> lines) {
		this.execProgram = lines;
	}	
	
	
	/*
	 * An assembly program is always in the following template
	 * <variables>
	 * <commands>
	 * Obs.
	 * 		variables names are always started with alphabetical char
	 * 	 	variables names must contains only alphabetical and numerical chars
	 *      variables names never uses any command name
	 * 		names ended with ":" identifies labels i.e. address in the memory
	 * 		Commands are only that ones known in the architecture. No comments allowed
	 * 	
	 * 		The assembly file must have the extention .dsf
	 * 		The executable file must have the extention .dxf 	
	 */
	


	/**
	 * This method reads an entire file in assembly 
	 * @param filename
	 * @throws IOException 
	 */
	public void read(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new		 
		   FileReader(filename+".dsf"));
		   lines.clear();
           String linha;
		   while ((linha = br.readLine()) != null) {
			     lines.add(linha);
			}
			br.close();
			
	}
	

	/**
	 * This method scans the strings in lines
	 * generating, for each one, the corresponding machine code
	 * @param lines
	 */
    public void parse() {
        objProgram.clear();
        variables.clear();
        labels.clear();
        labelsAdresses.clear();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split("\\s+");
            try {
                String name = tokens[0];
                boolean label = name.endsWith(":");
                if (label || (tokens.length == 1 && !isCommand(name))) {
                    if (label) name = name.substring(0, name.length() - 1);
                    if (tokens.length != 1 || !isSymbol(name) || isCommand(name)
                            || variables.contains(name) || labels.contains(name)) {
                        throw new IllegalArgumentException("Declaração inválida: " + line);
                    }
                    if (label) {
                        labels.add(name);
                        labelsAdresses.add(objProgram.size());
                    } else {
                        variables.add(name);
                    }
                } else {
                    proccessCommand(tokens);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Linha " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
    }

    private boolean isCommand(String name) {
        return commands.contains(name) || name.equals("move");
    }

    private boolean isSymbol(String value) {
        return value.matches("[A-Za-z][A-Za-z0-9]*");
    }

    // Uma linha por opcode de Architecture.decodeExecute().
    // R = registrador, M = endereço ou símbolo, I = inteiro imediato.
    private static final String[][] FORMS = {
        {"add", "M"}, {"sub", "M"}, {"jmp", "M"}, {"jz", "M"},
        {"jn", "M"}, {"read", "M"}, {"store", "M"}, {"ldi", "I"},
        {"inc", ""}, {"move", "RR"},
        {"add", "RR"}, {"add", "MR"}, {"add", "RM"},
        {"sub", "RR"}, {"sub", "IR"}, {"jmpMem", "M"},
        {"jnz", "M"}, {"jeq", "RRM"}, {"jlw", "RRM"}, {"inc", "R"},
        {"move", "RR"},
        {"move", "MR"}, // 21: moveRegMem() lê memória e escreve no registrador
        {"move", "RM"}, // 22: moveMemReg() lê registrador e escreve na memória
        {"move", "IR"}
    };

    protected void proccessCommand(String[] tokens) {
        int length = tokens.length;
        while (length > 1 && tokens[length - 1] == null) length--;
        for (int opcode = 0; opcode < FORMS.length; opcode++) {
            String mnemonic = FORMS[opcode][0];
            String format = FORMS[opcode][1];
            if (!tokens[0].equals(mnemonic) && !tokens[0].equals(commands.get(opcode))) continue;
            if (format.length() != length - 1) continue;

            ArrayList<String> words = new ArrayList<>();
            words.add(Integer.toString(opcode));
            for (int i = 1; i < length; i++) {
                String value = tokens[i];
                // Em "move 5 %RPG1", 5 é imediato. "move t %RPG1" lê a variável.
                if (opcode == 21 && tokens[0].equals("move") && !isSymbol(value) && i == 1) break;
                String word = encodeOperand(format.charAt(i - 1), value);
                if (word == null) break;
                words.add(word);
            }
            if (words.size() == length) {
                objProgram.addAll(words);
                return;
            }
        }
        throw new IllegalArgumentException("Instrução ou operandos inválidos: " + String.join(" ", tokens));
    }

    private String encodeOperand(char type, String value) {
        if (type == 'R') {
            return value.startsWith("%") && searchRegisterId(value.substring(1), arch.getRegistersList()) >= 0
                    ? value : null;
        }
        if (type == 'M' && isSymbol(value)) return "&" + value;
        if (!value.matches("[+-]?[0-9]+")) return null;
        int number = Integer.parseInt(value);
        if (type == 'M' && (number < 0 || number >= arch.getMemorySize())) return null;
        return Integer.toString(number);
    }

	/**
	 * This method creates the executable program from the object program
	 * Step 1: check if all variables and labels mentioned in the object 
	 * program are declared in the source program
	 * Step 2: allocate memory addresses (space), from the end to the begin (stack)
	 * to store variables
	 * Step 3: identify memory positions to the labels
	 * Step 4: make the executable by replacing the labels and the variables by the
	 * corresponding memory addresses 
	 * @param filename 
	 * @throws IOException 
	 */
	public void makeExecutable(String filename) throws IOException {
		if (!checkLabels())
            throw new IllegalArgumentException("Símbolo não declarado.");
        if (objProgram.size() + 1 + variables.size() > arch.getMemorySize())
            throw new IllegalArgumentException("Programa e variáveis excedem a memória.");
		execProgram = new ArrayList<>(objProgram);
		replaceAllVariables();
		replaceLabels(); //replacing all labels by the address they refer to
		replaceRegisters(); //replacing all registers by the register id they refer to
		saveExecFile(filename);
		System.out.println("Finished");
	}

	/**
	 * This method replaces all the registers names by its correspondings ids.
	 * registers names must be prefixed by %
	 */
	protected void replaceRegisters() {
		int p=0;
		for (String line:execProgram) {
			if (line.startsWith("%")){ //this line is a register
				line = line.substring(1, line.length());
				int regId = searchRegisterId(line, arch.getRegistersList());
				String newLine = Integer.toString(regId);
				execProgram.set(p, newLine);
			}
			p++;
		}
		
	}

	/**
	 * This method replaces all variables by their addresses.
	 * The addresses o0f the variables startes in the end of the memory
	 * and decreases (creating a stack)
	 */
	protected void replaceAllVariables() {
		int position = arch.getMemorySize()-1; //starting from the end of the memory
		for (String var : this.variables) { //scanning all variables
			replaceVariable(var, position);
			position --;
		}
	}

	/**
	 * This method saves the execFile collection into the output file
	 * @param filename
	 * @throws IOException 
	 */
	private void saveExecFile(String filename) throws IOException {
		File file = new File(filename+".dxf");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String l : execProgram)
			writer.write(l+"\n");
		writer.write("-1"); //-1 is a flag indicating that the program is finished
		writer.close();
		
	}

	/**
	 * This method replaces all labels in the execprogram by the corresponding
	 * address they refer to
	 */
	protected void replaceLabels() {
		int i=0;
		for (String label : labels) { //searching all labels
			label = "&"+label;
			int labelPointTo = labelsAdresses.get(i);
			int lineNumber = 0;
			for (String l : execProgram) {
				if (l.equals(label)) {//this label must be replaced by the address
					String newLine = Integer.toString(labelPointTo); // the address
					execProgram.set(lineNumber, newLine);
				}
				lineNumber++;
			}
			i++;
		}
		
	}

	/**
	 * This method replaces all occurences of a variable
	 * name found in the object program by his address
	 * in the executable program
	 * @param var
	 * @param position
	 */
	protected void replaceVariable(String var, int position) {
		var = "&"+var;
		int i=0;
		for (String s:execProgram) {
			if (s.equals(var)) {
				s = Integer.toString(position);
				execProgram.set(i, s);
			}
			i++;
		}
	}

	/**
	 * This method checks if all labels and variables in the object program were in the source
	 * program.
	 * The labels and the variables collection are used for this
	 */
	protected boolean checkLabels() {
		System.out.println("Checking labels and variables");
		for (String line:objProgram) {
			boolean found = false;
			if (line.startsWith("&")) { //if starts with "&", it is a label or a variable
				line = line.substring(1, line.length());
				if (labels.contains(line))
					found = true;
				if (variables.contains(line))
					found = true;
				if (!found) {
					System.out.println("FATAL ERROR! Variable or label "+line+" not declared!");
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * This method searches for a register in the architecture register list
	 * by the register name
	 * @param line
	 * @param registersList
	 * @return
	 */
	private int searchRegisterId(String line, ArrayList<Register> registersList) {
		int i=0;
		for (Register r:registersList) {
			if (line.equals(r.getRegisterName())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public static void main(String[] args) throws IOException {
		String filename = args[0];
		Assembler assembler = new Assembler();
		System.out.println("Reading source assembler file: "+filename+".dsf");
		assembler.read(filename);
		System.out.println("Generating the object program");
		assembler.parse();
		System.out.println("Generating executable: "+filename+".dxf");
		assembler.makeExecutable(filename);
	}
		
}