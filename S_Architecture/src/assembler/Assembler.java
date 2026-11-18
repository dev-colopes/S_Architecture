package assembler;

import architecture.Architecture;
import components.Register;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
		for (String s:lines) {
			String tokens[] = s.split(" ");
			if (findCommandNumber(tokens)>=0) { //the line is a command
				proccessCommand(tokens);
			}
			else { //the line is not a command: so, it can be a variable or a label
				if (tokens[0].endsWith(":")){ //if it ends with : it is a label
					String label = tokens[0].substring(0, tokens[0].length()-1); //removing the last character
					labels.add(label);
					labelsAdresses.add(objProgram.size());
				}
				else //otherwise, it must be a variable
					variables.add(tokens[0]);
			}
		}
		
	}



	/**
	 * This method processes a command, putting it and its parameters (if they have)
	 * into the final array
	 * @param tokens
	 */
	protected void proccessCommand(String[] tokens) {
		int opcode = findCommandNumber(tokens);
		String command = commands.get(opcode);
		String format;
		switch (command) {
		case "inc": format = ""; break;
		case "incReg": format = "R"; break;
		case "ldi": format = "I"; break;
		case "addRegReg": case "subRegReg": case "moveRegReg":
			format = "RR"; break;
		case "addMemReg": case "moveRegMem":
			format = "MR"; break;
		case "addRegMem": case "moveMemReg":
			format = "RM"; break;
		case "moveImmReg": case "subImmReg":
			format = "IR"; break;
		case "jeq": case "jlw":
			format = "RRM"; break;
		default: // add, sub, read, store e desvios com um endereço
			format = "M";
		}

		int length = tokens.length;
		while (length > 1 && tokens[length - 1] == null) length--;
		if (length != format.length() + 1) throw invalidCommand(tokens);
		ArrayList<String> words = new ArrayList<>();
		words.add(Integer.toString(opcode));
		for (int i = 1; i < length; i++) {
			String value = tokens[i];
			switch (format.charAt(i - 1)) {
			case 'R':
				if (!value.startsWith("%") ||
					searchRegisterId(value.substring(1), arch.getRegistersList()) < 0)
					throw invalidCommand(tokens);
				break;
			case 'I':
				value = Integer.toString(Integer.parseInt(value));
				break;
			case 'M':
				if (value.matches("[A-Za-z][A-Za-z0-9]*")) {
					value = "&" + value;
				} else {
					int address = Integer.parseInt(value);
					if (address < 0 || address >= arch.getMemorySize()) throw invalidCommand(tokens);
					value = Integer.toString(address);
				}
				break;
			}
			words.add(value);
		}
		objProgram.addAll(words);
	}

	private int findCommandNumber(String[] tokens) {
		String command = tokens[0];
		boolean twoOperands = tokens.length >= 3 && tokens[2] != null;
		if ("move".equals(command)) return proccessMove(tokens);
		if ("add".equals(command) && twoOperands) {
			if (tokens[1].startsWith("%"))
				command = tokens[2].startsWith("%") ? "addRegReg" : "addRegMem";
			else
				command = "addMemReg";
		}
		if ("sub".equals(command) && twoOperands)
			command = tokens[1].startsWith("%") ? "subRegReg" : "subImmReg";
		if ("inc".equals(command) && tokens.length > 1 && tokens[1] != null)
			command = "incReg";
		return commands.indexOf(command);
	}

	private int proccessMove(String[] tokens) {
		if (tokens.length < 3 || tokens[2] == null) throw invalidCommand(tokens);
		if (tokens[1].startsWith("%")) {
			if (tokens[2].startsWith("%")) return commands.indexOf("moveRegReg");
			return commands.indexOf("moveMemReg"); // Opcode 22: registrador para memória.
		}
		if (tokens[1].matches("[+-]?[0-9]+")) return commands.indexOf("moveImmReg");
		return commands.indexOf("moveRegMem"); // Opcode 21: memória para registrador.
	}

	private IllegalArgumentException invalidCommand(String[] tokens) {
		return new IllegalArgumentException("Operandos inválidos: " + String.join(" ", tokens));
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
			return;
		execProgram = (ArrayList<String>) objProgram.clone();
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
