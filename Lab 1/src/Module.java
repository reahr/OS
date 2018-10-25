import java.util.ArrayList;

/*
* Name: Reah Rajmangal
* The Module class parses the file into according lists.
* Those lists are then checked for errors while  relocating relative addresses, and resolving external references.
 */

//package private-default
class Module {
    ArrayList<Symbol> symbolsDefined = new ArrayList<>();
    ArrayList<String> symbolsUsedInModule = new ArrayList<>();

    ArrayList<Instruction> programText;
    ArrayList<String> usedSymbol = new ArrayList<>();

    int baseAddress;
    int moduleNumber;
    int lengthOfModule;

    /**
     * @param moduleNumber The number module this is indexed at, starting base-0
     * @param baseAddress  The number of instructions that have already been parsed and hence relocation necessary
     */
    Module(int moduleNumber, int baseAddress) {
        this.moduleNumber = moduleNumber;
        this.baseAddress = baseAddress;
    }

    /**
     * Sets this.programText which consist of all instructions for this module
     *
     * @param programText An ArrayList generated from file which has determined program list
     * @return An ArrayList of Instructions of which each contains an instruction and an address
     */
    ArrayList<Instruction> setProgramText(ArrayList<String> programText) {
        ArrayList<Instruction> instructionsInModule = new ArrayList<>();
        for (int i = 0; i < programText.size(); i += 2) {
            Instruction x = new Instruction(programText.get(i), programText.get(i + 1));
            instructionsInModule.add(x);
        }
        this.programText = instructionsInModule;
        return instructionsInModule;
    }

    /**
     * A long method that checks instructions and relocates relative address and resolves external references
     *
     * @param symbols       An ArrayList of Symbols which will be referenced when resolving external references
     * @param sizeOfMachine A number that counts the total number of instructions thus far, and relocates rel addresses
     * @return A ArrayList log that consist of information that should be written with the Memory map or after it
     */
    ArrayList<ArrayList<String>> resolveAddress(ArrayList<Symbol> symbols, int sizeOfMachine) {
        ArrayList<ArrayList<String>> logs = new ArrayList<>();
        ArrayList<String> inMemMap = new ArrayList<>(); //show as we add to memory map
        ArrayList<String> afterMM = new ArrayList<>(); //show after memory map

        for (int i = 0; i < programText.size(); i++) {
            Instruction instr = programText.get(i);
            int address = Integer.parseInt(instr.address.substring(1));
            if (instr.instruction.equals("R")) {
                if (address > lengthOfModule) {
                    instr.address = instr.address.charAt(0) + "000";
                    instr.instruction = "A";
                    inMemMap.add(String.format("%2d:%6s Error: Relative address exceeds module size; zero used.",
                            baseAddress + i, instr.address));
                } else {
                    int instructionAddress = Integer.parseInt(instr.address);
                    String numberAsString = String.format("%03d", instructionAddress += baseAddress);
                    inMemMap.add(String.format("%2d:%6s", baseAddress + i, numberAsString));
                }
            } else if (instr.instruction.equals("E")) {
                //check if external address is too large
                int sizeOfUseList = symbolsUsedInModule.size();
                if (address > sizeOfUseList) {
                    instr.instruction = "I";
                    inMemMap.add(String.format("%2d:%6s Error: External address exceeds length of use list; treated as immediate.",
                            baseAddress + i, instr.address));
                } else {
                    //check if not defined at all
                    String symbolUsed = symbolsUsedInModule.get(address);
                    Symbol x = new Symbol(symbolUsed, 0, 0);
                    if (!symbols.contains(x)) {
                        instr.address = instr.address.charAt(0) + "000";
                        inMemMap.add(String.format("%2d:%6s Error: %s is not defined; zero used.",
                                baseAddress + i, instr.address, symbolUsed));
                    } else {
                        Symbol symbol = symbols.get(symbols.indexOf(x));
                        symbol.setUsed(moduleNumber);
                        usedSymbol.add(symbol.name);
                        String numberAsString = String.format("%03d", symbol.address);
                        inMemMap.add(String.format("%2d:%6s", baseAddress + i, instr.address.charAt(0) + numberAsString));
                    }
                }
            } else if (instr.instruction.equals("A")) {
                //check if absolute address > size machine
                if (address > sizeOfMachine) {
                    instr.address = instr.address.charAt(0) + "000";
                    inMemMap.add(String.format("%2d:%6s Error: Absolute address exceeds machine size; zero used.",
                            baseAddress + i, instr.address));
                } else {
                    inMemMap.add(String.format("%2d:%6s", baseAddress + i, instr.address));
                }
            } else {
                //Immediate
                inMemMap.add(String.format("%2d:%6s", baseAddress + i, instr.address));
            }
        }

        for (int i = 0; i < symbolsUsedInModule.size(); i++) {
            Symbol x = new Symbol(symbolsUsedInModule.get(i), 0, 0);
            Symbol symbol;
            try {
                symbol = symbols.get(symbols.indexOf(x));
            } catch (IndexOutOfBoundsException e) {
                //symbol could not be found
                continue;
            }
            if (symbol.used != moduleNumber) {
                afterMM.add(String.format("Warning: In module %d %s appeared in the use list but was not actually used.", moduleNumber, symbolsUsedInModule.get(i)));
            }
        }
        logs.add(inMemMap);
        logs.add(afterMM);
        return logs;
    }
}

/**
 * The Instruction class creates an Instruction that checks if Instruction is correct based on specification
 */
class Instruction {
    String instruction;
    String address;

    /**
     * @param instruction (I)mmediate, (A)bsolute, (R)elative, or (E)xternal
     * @param address     4-digit word
     * @throws IllegalArgumentException if Invalid address type or instruction
     */
    Instruction(String instruction, String address) {
        if (!instruction.equals("R") && !instruction.equals("E") && !instruction.equals("A")
                && !instruction.equals("I")) {
            throw new IllegalArgumentException("Invalid address type used: " + instruction + ".");
        }
        this.instruction = instruction;
        if (address.length() == 4) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Invalid word used: '" + address + "' is not of length 4.");
        }
    }

    /**
     * A String representation of  this Instruction
     *
     * @return String that is correct address (resolved/relocated)
     */
    @Override
    public String toString() {
        return (instruction + ":" + address + "\n");
    }
}

