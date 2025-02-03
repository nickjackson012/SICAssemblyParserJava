import java.util.*;

public class ParseAssemblyCode {
    public static ArrayList<ParsedLineOfCode> parse(Scanner fileReader) throws SICParsingError {
            ArrayList<ParsedLineOfCode> parsedCodeList = new ArrayList<>();
            int lineNumber = 0;
            boolean startFound = false;
            boolean endFound = false;
            while (fileReader.hasNextLine()){
                // Populate unparsed line of code and line number attributes
                String unparsedLineOfCode = fileReader.nextLine();
                lineNumber += 1;
                ParsedLineOfCode lineOfCode = new ParsedLineOfCode();
                lineOfCode.setLineNumber(lineNumber);
                lineOfCode.setUnparsedLineOfCode(unparsedLineOfCode);
                parsedCodeList.add(lineOfCode);

                // Check for empty line in the Assembly Code File
                if (unparsedLineOfCode.isBlank()){
                    // Close assembly code file, print error, and exit program
                    fileReader.close();
                    throw new SICParsingError("Parser Error: Line " + lineOfCode.getLineNumber() + " is blank.");
                }

                // Check for comment indicator(".") in the line of code
                // "." must be the first non-whitespace character
                // in order for a line of code to be a comment.
                if (unparsedLineOfCode.trim().indexOf(".") == 0){
                    lineOfCode.setIsComment(true);
                    continue;
                }

                // Populate remaining attributes: label, opcode, operand
                // Handle label
                // If the 0 index in the unparsed line of code is alphabetical,
                // the first token will be the label
                boolean hasLabel = false;
                char firstCharacter = unparsedLineOfCode.charAt(0);
                if (Character.isAlphabetic(firstCharacter)){
                    hasLabel = true;
                }

                // Handle Byte Character Strings,
                // parse out expected tokens,
                // and count the number of tokens.
                String byteCharacterString = lineOfCode.get_byte_character_string(unparsedLineOfCode);

                // Tokenize the unparsed line of code.
                // "\\s+" will split the string on any whitespace character (space, tab, etc.)
                ArrayList<String> tokenList = new ArrayList<>(Arrays.asList(unparsedLineOfCode.trim().split("\\s+")));
                if (byteCharacterString != null){
                    tokenList = lineOfCode.handleByteCharacterString(tokenList, byteCharacterString);
                }

                // Handle opcode
                // If the line of code has a label, then the second token is the opcode
                // if the line of code doesn't have a label the first token is the opcode
                try {
                    if (hasLabel && tokenList.size() >= 3) {
                        lineOfCode.setLabel(tokenList.get(0));
                        lineOfCode.setOpcode(tokenList.get(1));
                        if (!Objects.equals(lineOfCode.getOpcode(), "RSUB")
                                && !Objects.equals(lineOfCode.getOpcode(), "END")
                                && !Objects.equals(lineOfCode.getOpcode(), "XOS")) {
                            //System.out.println("line number: " + lineOfCode.getLineNumber() + "\nopcode: " + lineOfCode.getOpcode());
                            lineOfCode.setOperand(tokenList.get(2));
                        }
                    } else if (hasLabel && tokenList.size() >= 2) {
                        lineOfCode.setLabel(tokenList.get(0));
                        lineOfCode.setOpcode(tokenList.get(1));
                    } else if (!hasLabel && tokenList.size() >= 2) {
                        lineOfCode.setOpcode(tokenList.get(0));
                        if (!Objects.equals(lineOfCode.getOpcode(), "RSUB")
                                && !Objects.equals(lineOfCode.getOpcode(), "END")
                                && !Objects.equals(lineOfCode.getOpcode(), "XOS")) {
                            lineOfCode.setOperand(tokenList.get(1));
                        }
                    } else if (!hasLabel && tokenList.size() == 1) {
                        lineOfCode.setLoneOpcode(tokenList.get(0));
                    } else {
                        throw new SICParsingError("Line of code cannot be parsed");
                    }
                }

                catch (SICParsingError ex){
                    fileReader.close();

                    throw new SICParsingError("Parser Error: " + ex.getMessage() + "\n" +
                            "LINE " + lineOfCode.getLineNumber() + ": " + lineOfCode.getUnparsedLineOfCode());
                }

                if (!startFound){
                    if (lineOfCode.getOpcode().equals("START")){
                        startFound = true;
                    }

                    else{
                        fileReader.close();

                        throw new SICParsingError("Parser Error: START must be the first opcode called in assembly program.\n"
                                                 + "LINE " + lineOfCode.getLineNumber() + ": " + lineOfCode.getUnparsedLineOfCode());
                    }
                }

                if (lineOfCode.getOpcode().equals("END")){
                    endFound = true;
                    break;
                }
            }

            fileReader.close();

            if (endFound){
              System.out.println("Parsing complete.");
            }

            else{
                fileReader.close();

                throw new SICParsingError("Parser Error: No END assembly directive found.");
            }

            return parsedCodeList;
    }

    /*
    public static void main(String[] args) throws SICParsingError {
        try {
            File assemblyCodeFile = new File("/Users/nickjackson/Desktop/Assembly Code/ReadWriteTest.asm");
            //File assemblyCodeFile = new File("/Users/nickjackson/Desktop/Assembly Code/ReadWrite.asm");
            System.out.println("Assembly Code file found.");
            Scanner fileReader = new Scanner(assemblyCodeFile);

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
        } catch (FileNotFoundException e) {
            throw new SICParsingError("Assembly Code file not found.");
        }
    }
     */
}