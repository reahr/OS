import java.io.*;
import java.util.*;

/**
 * Name: Reah Rajmangal
 * This is the main file that should be used to run the program, takes an argument file
 */

public class TwoPassLinker {

    //Symbol class gathers all symbols in files
    static ArrayList<Symbol> symbols = Symbol.symbols;

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1){
            System.err.printf("ERROR: the program expects a file name as an argument.\n");
            System.err.printf("Usage: java TwoPassLinker [fileName]\n" );
            System.exit(1);
        }

        String path = args[0];
        File file = new File(path);

        if (!file.exists()) {
            System.err.printf("ERROR: file %s does not exist.\n", args[0]);
            System.exit(1);
        }

        if (!file.canRead()) {
            System.err.printf("ERROR: file %s cannot be read.\n", args[0]);
        }

        //check if file is empty based on size
        if (file.length() == 0) {
            System.err.printf("ERROR: %s is empty.", args[0]);
            System.exit(1);
        }

        Scanner scanner = new Scanner(file);
        int totalModules = Integer.parseInt(scanner.next());
        int numOfModule = 0;
        int numOfInstructions = 0; //length of machine
        ArrayList<Module> modules = new ArrayList<>();
        String y = "";

        while (scanner.hasNext()) {
            Module m = new Module(numOfModule, numOfInstructions);

            //parsing some String to ints, may throw an NumberFormatException
            try {
                //this to catch if symbol definition not valid
                try {
                    //Symbol list
                    int numOfSymbols = Integer.parseInt(scanner.next());
                    for (int i = 0; i < numOfSymbols; i++) {
                        Symbol x = new Symbol(scanner.next(), Integer.parseInt(scanner.next()), numOfModule);
                        m.symbolsDefined.add(x);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Symbol in module " + numOfModule + " does not have a valid definition");
                }

                //Use list
                int numOfUsages = Integer.parseInt(scanner.next());
                ArrayList<String> usages = new ArrayList<>();
                for (int i = 0; i < numOfUsages; i++) {
                    usages.add(scanner.next());
                }
                m.symbolsUsedInModule = usages;

                //Program Text
                int numOfInstructionsInModule = Integer.parseInt(scanner.next());
                numOfInstructions += numOfInstructionsInModule;
                m.lengthOfModule = numOfInstructionsInModule;
                ArrayList<String> programText = new ArrayList<>();
                for (int i = 0; i < numOfInstructionsInModule * 2; i++) {
                    programText.add(scanner.next());
                }

                //now we check if the symbol addresses defined in module is > length of module (bc we have the length now)
                for (int i = 0; i < m.symbolsDefined.size(); i++) {
                    Symbol x = m.symbolsDefined.get(i);
                    if (x.address > numOfInstructionsInModule) {
                        x.address = m.baseAddress;
                        symbols.add(x);
                        y = "Error: In module " + numOfModule + " the def of " + x.name +
                                " exceeds the module size; zero (relative) used.";
                    } else {
                        if (symbols.contains(x)) {
                            symbols.get(symbols.indexOf(x)).setDuplicate();
                        } else {
                            x.address += m.baseAddress;
                            symbols.add(x);
                        }
                    }

                }
                m.setProgramText(programText); //will create Instruction type for each pair of instructions
                modules.add(m);
                numOfModule++; //to keep track of which module we are in

                if (numOfInstructions > 200) throw new Exception("Warning: Size of machine is greater than 200.");

            } catch (NumberFormatException e) {
                System.err.println("Cannot parse data accordingly, check format of lists.");
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        scanner.close();
        PrintStream output = System.out;

        //checking for any possible parsing errors, we parse some strings to ints in the Module class
        try {
            output.println("Symbol Table");
            for (int i = 0; i < symbols.size(); i++) {
                output.print(symbols.get(i));
            }

            output.println("\nMemory Map");
            ArrayList<String> afterMM = new ArrayList<>();

            //second pass, we grab module info (use list and program txt and use accordingly)
            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                ArrayList<ArrayList<String>> logs = m.resolveAddress(symbols, numOfInstructions);
                for (int j = 0; j < logs.get(0).size(); j++) {
                    output.println(logs.get(0).get(j));
                }
                for (int j = 0; j < logs.get(1).size(); j++) {
                    afterMM.add(logs.get(1).get(j));
                }

            }

            output.print("\n");
            for (int i = 0; i < afterMM.size(); i++) {
                output.println(afterMM.get(i) + "\n");
            }
            for (int i = 0; i < symbols.size(); i++) {
                Symbol x = symbols.get(i);
                if (x.used == -1) {
                    String warning = String.format("Warning: %s was defined in module %s but never used.\n", x.name, x.moduleNumber);
                    output.println(warning);
                }
            }
            if (!y.isEmpty()) output.println(y + "\n");
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse data accordingly, check format of list elements.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        output.close();
    }
}