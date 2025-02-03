import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsedLineOfCode{
    private static final int MAXIMUM_START_OPERAND_LENGTH = 4;
    private static final int MINIMUM_MEMORY_ADDRESS_DEC = 0;
    private static final int MAXIMUM_MEMORY_ADDRESS_DEC = 32767;
    private static final int MINIMUM_BYTE_OPERAND_LENGTH = 1;
    private static final int MAXIMUM_BYTE_OPERAND_LENGTH = 32;
    private static final int MINIMUM_INTEGER = -8388608;
    private static final int MAXIMUM_INTEGER = 8388607;
    private static final int MINIMUM_RESB_VALUE = 1;
    private static final int MAXIMUM_RESB_VALUE = 32768;
    private static final int MINIMUM_RESW_VALUE = 1;
    private static final int MAXIMUM_RESW_VALUE = 10922;
    private static final int MAXIMUM_LENGTH_OF_OPERAND = 5;
    private int lineNumber;
    private String label;
    private String opcode;
    private String operand;
    private String unparsedLineOfCode;
    private boolean isComment = false;


    public int getLineNumber(){
        return lineNumber;
    }

    public void setLineNumber(int lineNumber){
        this.lineNumber = lineNumber;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label) throws SICParsingError {
        this.label = validateLabel(label);
    }

    public void setLoneOpcode(String loneOpcode) throws SICParsingError {
        this.opcode = validateLoneOpcode(loneOpcode);
    }

    public String getOpcode(){
        return opcode;
    }

    public void setOpcode(String opcode) throws SICParsingError {
        this.opcode = validateOpcode(opcode);
    }

    public String getOperand(){
        return operand;
    }

    public void setOperand(String operand) throws SICParsingError {
        this.operand = validateOperand(operand, this.opcode);
    }

    public String getUnparsedLineOfCode(){
        return unparsedLineOfCode;
    }

    public void setUnparsedLineOfCode(String lineOfCode){
        this.unparsedLineOfCode = lineOfCode;
    }

    public boolean getIsComment(){
        return isComment;
    }

    public void setIsComment(boolean isComment){
        this.isComment = isComment;
    }

    // This function validates label tokens against defined label rules:
    // 1) Labels can only contain uppercase (A-Z) and numeric (0-9) characters
    // 2) The first character in a label must be uppercase (A-Z)
    // 3) Labels must be 1 to 6 characters in length
    private String validateLabel(String label) throws SICParsingError {
        char firstCharacter = label.charAt(0);
        // Label must be Uppercase and Alphanumeric
        boolean charactersValid = label.equals(label.toUpperCase()) && label.matches("[A-Z0-9]+");
        // The first character in the label must be alphabetical
        boolean firstCharacterValid = Character.isAlphabetic(firstCharacter);
        // The label can not be longer than 6 characters
        boolean lengthValid = label.length() <= 6;
        
        if (charactersValid && firstCharacterValid && lengthValid){
            return label;
        }
        else{
            throw new SICParsingError("Invalid Label.");
        }
    }

    // This function validates opcode tokens
    // against a set of valid opcodes.
    private String validateOpcode(String opcode) throws SICParsingError {
      boolean opcodeValid = false;
      List<String> opcodeValidationSet = Arrays.asList("ADD", "AND", "COMP", "DIV", "J", "JEQ", "JGT",
                                                    "JLT", "JSUB", "LDA", "LDCH", "LDL", "LDX", "MUL",
                                                    "OR", "RD", "RSUB", "STA", "STCH", "STL", "STSW",
                                                    "STX", "SUB", "TD", "TIX", "WD", "START", "END",
                                                    "BYTE", "WORD", "RESB", "RESW");

      if (opcodeValidationSet.contains(opcode)){
          return opcode;
      }
      else{
          throw new SICParsingError("Opcode is invalid.");

      }
    }

    // This function validates opcode tokens
    // against a set of valid opcodes
    private String validateLoneOpcode(String opcode) throws SICParsingError {
        List<String> loneOpcodeValidationSet = Arrays.asList("RSUB", "END");

        if (loneOpcodeValidationSet.contains(opcode)){
            return opcode;
        }

        else{
            throw new SICParsingError("Lone opcode is invalid.");
        }
    }

    // This function validates the START operand.
    // RULES:
    //      1) Operand must be a valid memory address hex string (0000 - 7FFF).
    //         No additional decoration should be present.
    private String validateSTARTOperand(String operand) throws SICParsingError {
        Integer decValue = null;

        if (operand.length() > MAXIMUM_START_OPERAND_LENGTH){
            throw new SICParsingError("Start address must be between 0000-7FFF");
        }

        try{
            decValue = Integer.parseInt(operand, 16);
        }

        catch (NumberFormatException ne){
            throw new SICParsingError("Start address must be between 0000-7FFF");
        }

        if (decValue < MINIMUM_MEMORY_ADDRESS_DEC || decValue > MAXIMUM_MEMORY_ADDRESS_DEC){
            throw new SICParsingError("Start address must be between 0000-7FFF");
        }

        return operand;
    }

    // This method validates the BYTE operand.
    // RULES:
    //      1) Operand can be either a hex string or a string or characters
    //      2A.) If operand is a hex string it must be formed as such "X'hhhh...'" (h = valid hex digit string)
    //      2B.) The hex digit string must contain an even number of characters and must be 2-32 characters in length.
    //      3A.) If operand is a character string it must be formed as such "C'aaaa...'" (a = valid ascii character)
    //      3B.) The character string must contain an even number of characters and must be  1-32 characters in length.
    private String validateBYTEOperand(String operand) throws SICParsingError {
        List<String> hexDigitSet = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F");

        // Test for valid string length
        if (operand.length() < 4){
            throw new SICParsingError("Invalid BYTE operand");
        }

        String byteOperandSubstring = operand.substring(operand.indexOf("'") + 1, operand.lastIndexOf("'"));

        if (operand.indexOf("'") == 1 && operand.lastIndexOf("'") == (operand.length() - 1)){
            if (byteOperandSubstring.isEmpty() || byteOperandSubstring.length() > MAXIMUM_BYTE_OPERAND_LENGTH){
               throw new SICParsingError("Operand must have 1-32 characters between quotations");
            }

            if (operand.startsWith("X")){
                // Handle hex
                if (byteOperandSubstring.length() % 2 == 0){
                    for (int index = 0; index < byteOperandSubstring.length(); index++){
                        char hexDigit = byteOperandSubstring.charAt(index);
                        if (!hexDigitSet.contains(Character.toString(hexDigit))){
                            throw new SICParsingError("Operand must contain valid hex value.");
                        }
                    }
                    return operand;
                }
                else{
                    throw new SICParsingError("Operand must contain an even number of hex digits.");
                }
            }

            else if (operand.startsWith("C")){
                // Handle characters
                if (byteOperandSubstring.matches("\\A\\p{ASCII}*\\z")){
                    return operand;
                }

                else{
                    throw new SICParsingError("Operand must contain ascii characters.");
                }
            }

            else{
               throw new SICParsingError("Valid format indicator(C or X) required in Operand");
            }
        }
        else{
            throw new SICParsingError("Single quotation marks missing from OPERAND");
        }
    }

    // This function validates the WORD operand.
    // RULES:
    //  1.) Operand can be a decimal integer in the supported SIC architecture range.
    //        -8,388,608 to 8,388,607
    private String validateWORDOperand(String operand) throws SICParsingError {
       Integer wordValue = null;

        try{
            wordValue = Integer.parseInt(operand);
        }
        catch (NumberFormatException e){
            throw new SICParsingError("WORD operand must be a decimal integer");
        }

        if (wordValue >= MINIMUM_INTEGER && wordValue <= MAXIMUM_INTEGER){
            return operand;
        }

        else{
            throw new SICParsingError("WORD operand must be in the range of -8,388,608 to 8,388,607");
        }
    }

    // This function validates the RESB operand.
    // RULES:
    // NOTE: this is a very loose restriction. It will not protect against reserving more memory than what is available.
    //    1.) Operand must be a positive decimal integer
    //    2.) Operand must be less than the number of possible bytes in memory(32,768).
    private String validateRESBOperand(String operand) throws SICParsingError {
        Integer resbValue = null;

        try{
            resbValue = Integer.parseInt(operand);
        }

        catch (NumberFormatException e) {
            throw new SICParsingError("RESB operand must be a positive decimal integer");
        }
        if (resbValue >= MINIMUM_RESB_VALUE && resbValue <= MAXIMUM_RESB_VALUE){
            return operand;
        }

        else{
            throw new SICParsingError("RESB operand must be between 0 and 32768.");
        }
    }

    // This function validates the RESW operand.
    // RULES:
    // NOTE: this is a very loose restriction. It will not protect against reserving more memory than what is available.
    //    1.) Operand must be a positive decimal integer
    //    2.) Operand must be less than the number of possible words in memory(10,922).
    private String validateRESWOperand(String operand) throws SICParsingError {
        Integer reswValue = null;

        try{
            reswValue = Integer.parseInt(operand);
        }

        catch (NumberFormatException e) {
            throw new SICParsingError("RESW operand must be a positive decimal integer");
        }

        if (reswValue >= MINIMUM_RESW_VALUE && reswValue <= MAXIMUM_RESW_VALUE){
            return operand;
        }

        else{
            throw new SICParsingError("RESW operand must be between 0 and 10922.");
        }
    }

    // This method validates the operand for all other opcodes.
    // RULES:
    //    1.) Operand can be a string that follows label rules (see validate_label() for label rules)
    //    2.) Operand can be a valid hex memory address string padded with "0" as the first character.
    //           a max of 5 total characters ex. "0hhhh" (h = valid hex value)
    //    3.) Each of the above may include the indexed addressing indicator(",X")
    //    as the last two characters in the string
    private String validateNonspecificOperand(String operand) throws SICParsingError {
        char firstCharacter = operand.charAt(0);
        Integer decValue = null;
        String indexAddressingCheck = operand.substring(operand.length() - 2);

        if (indexAddressingCheck.equals(",X")){
            operand = operand.substring(0, operand.length() - 2);
        }

        if (Character.isAlphabetic(firstCharacter)){
            //Operand will be validated as a label
            boolean charactersValid = operand.equals(operand.toUpperCase()) && operand.matches("[A-Z0-9]+");
            boolean lengthValid = operand.length() <= 6;

            if (charactersValid && lengthValid){
                return operand;
            }

            else{
                throw new SICParsingError("Operand must be formatted as a label.");
            }
        }

        else if (firstCharacter == 0){
            // Operand will be validated as a hex memory address.
            String errorMessage = "Operand memory address must be between 00000-07FFF.";

            if (operand.length() > MAXIMUM_LENGTH_OF_OPERAND){
                throw new SICParsingError(errorMessage);
            }

            try{
                decValue = Integer.parseInt(operand, 16);
            }

            catch (NumberFormatException ne){
                throw new SICParsingError(errorMessage);
            }

            if (decValue < MINIMUM_MEMORY_ADDRESS_DEC || decValue > MAXIMUM_MEMORY_ADDRESS_DEC){
                throw new SICParsingError(errorMessage);
            }

            int requiredLeadingZeroes = MAXIMUM_LENGTH_OF_OPERAND - operand.length();

            return "0".repeat(requiredLeadingZeroes) + operand;
        }

        else{
            throw new SICParsingError("Operand must be a hex memory address or a label.");
        }
    }

    private String validateOperand(String operand, String opcode) throws SICParsingError {
        switch (opcode){
            case "START":
                operand = validateSTARTOperand(operand);
                break;
            case "END":
                // END opcode is not used.
                // We will ignore it and consider the END opcode the last instruction
                // in the assembly code program.
            case "BYTE":
                operand = validateBYTEOperand(operand);
                break;
            case "WORD":
                operand = validateWORDOperand(operand);
                break;
            case "RESB":
                operand = validateRESBOperand(operand);
                break;
            case "RESW":
                operand = validateRESWOperand(operand);
                break;
            default:
                operand = validateNonspecificOperand(operand);
        }

        return operand;
    }

        // This function parses and returns a BYTE character string
    // if one exists in the line of code, otherwise it returns null
    public String get_byte_character_string(String lineOfCode){
        String byteCharacterString = null;

        int startIndex = lineOfCode.indexOf("C'");

        if (startIndex != -1){
            byteCharacterString = lineOfCode.substring(startIndex+2);
            int endIndex = byteCharacterString.indexOf("'");
            if (endIndex != -1){
                byteCharacterString = byteCharacterString.substring(0 , endIndex);
                byteCharacterString = "C'" + byteCharacterString + "'";
            }
        }
        return byteCharacterString;
    }

    // This function handles BYTE character strings.
    // This special handling is required when there are
    // spaces in the BYTE character string that would
    // be tokenized otherwise.
    public ArrayList<String> handleByteCharacterString(ArrayList<String> tokenList, String byteCharacterString){
        ArrayList<String> parsedTokenList = new ArrayList<>();
        try{
            int byteOpcodeIndex = tokenList.indexOf("BYTE");
            int tokenListLength = tokenList.size();

            if (byteOpcodeIndex == 0 && tokenListLength >= 2){
                int existsIndex = tokenList.get(byteOpcodeIndex + 1).indexOf("C'");

                if (existsIndex != -1){
                    parsedTokenList.add(tokenList.get(0));
                    parsedTokenList.add(byteCharacterString);
                }
            }

            else if (byteOpcodeIndex == 1 && tokenListLength >= 3){
                int existsIndex = tokenList.get(byteOpcodeIndex + 1).indexOf("C'");

                if (existsIndex != -1){
                    parsedTokenList.add(tokenList.get(0));
                    parsedTokenList.add(tokenList.get(1));
                    parsedTokenList.add(byteCharacterString);
                }
            }
        }
        catch (IllegalArgumentException _){
        }

        return parsedTokenList;
    }
}

