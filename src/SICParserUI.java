import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class SICParserUI {
    private static final String SICPARSER_PROMPT = "SICParser> ";
    private static final String PARSE_MENU = "(p)arse, (q)uit";
    private static final String QUIT_CONFIRM = "Are you sure you want to quit? (y)es, (n)o";
    private static final String UNRECOGNIZED_COMMAND = "Unrecognized command";
    private static final String filePath = "/Users/nickjackson/Desktop/Assembly Code/";
    public static void main(String[] args) throws SICParsingError {
        // TODO: move constants to outside of method and make them private and static
        System.out.println("SIC PARSER");

        while (true){
            Scanner inputReader = new Scanner(System.in);
            System.out.println(PARSE_MENU);
            System.out.print(SICPARSER_PROMPT);
            String command = inputReader.nextLine();

            switch (command.toUpperCase()){
                case "P":
                    try {
                        System.out.println("Enter program file name with assembly code file extension");
                        System.out.print(SICPARSER_PROMPT);
                        String programFileName = inputReader.nextLine();
                        File assemblyCodeFile = new File(filePath + programFileName);
                        Scanner fileReader = new Scanner(assemblyCodeFile);
                        System.out.println("Assembly Code file found.");

                        ArrayList<ParsedLineOfCode> parsedCodeList = ParseAssemblyCode.parse(fileReader);


                        for (ParsedLineOfCode parsedLineOfCode : parsedCodeList) {
                            System.out.println("Line Number: " + parsedLineOfCode.getLineNumber());
                            System.out.println("Unparsed Line of Code: " + parsedLineOfCode.getUnparsedLineOfCode());
                            System.out.println("Is Comment: " + parsedLineOfCode.getIsComment());
                            System.out.println("Label: " + parsedLineOfCode.getLabel());
                            System.out.println("Opcode: " + parsedLineOfCode.getOpcode());
                            System.out.println("Operand: " + parsedLineOfCode.getOperand() + "\n");
                        }

                        fileReader.close();
                    }
                    catch (FileNotFoundException | SICParsingError e) {
                        throw new SICParsingError("Assembly Code file not found.");
                    }
                    break;

                case "Q":
                    System.out.println(QUIT_CONFIRM);
                    String quitCommand = inputReader.nextLine();

                    if (quitCommand.equalsIgnoreCase("Y")){
                        System.exit(0);
                    }
                    break;
                default:
                    System.out.println(UNRECOGNIZED_COMMAND);
            }

        }
    }

}
