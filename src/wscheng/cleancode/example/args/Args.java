package wscheng.cleancode.example.args;

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    private String[] args;
    private boolean valid;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> booleanArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> stringArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> intArgs = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private int currentArgument;
    private char errorArgument = '\0';
    private String errorParameter;

    enum ErrorCode {
        OK, MISSING_STRING, INVALID_INTEGER, MISSING_INTEGER
    }

    private ErrorCode errorCode = ErrorCode.OK;

    public Args(String schema, String[] args) throws ParseException {
        this.schema = schema;
        this.args = args;
        valid = parse();
    }

    public boolean isValid() {
        return valid;
    }

    public boolean parse() throws ParseException {
        if (schema.length() == 0 && args.length == 0) {
            return true;
        }
        parseSchema();
        parseArguments();
        return valid;
    }

    private boolean parseSchema() throws ParseException {
        for (String element : schema.split(",")) {
            if (element.length() > 0) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }
        }
        return true;
    }

    private void parseSchemaElement(String element) throws ParseException {
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);

        if (isBooleanSchemaElement(elementTail)) {
            parseBooleanSchemaElement(elementId);
        } else if (isStringSchemaElement(elementTail)) {
            parseStringSchemaElement(elementId);
        } else if (isIntSchemaElement(elementTail)) {
            parseIntSchemaElement(elementId);
        }
    }

    private void validateSchemaElementId(char elementId) throws ParseException {
        if (!Character.isLetter(elementId)) {
            throw new ParseException("Bad character:" + elementId + "in Args format: " + schema, 0);
        }
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.length() == 0;
    }

    private void parseBooleanSchemaElement(char elementId) {
        booleanArgs.put(elementId, new BooleanArgumentMarshaler());
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private void parseStringSchemaElement(char elementId) {
        stringArgs.put(elementId, new StringArgumentMarshaler());
    }

    private boolean isIntSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private void parseIntSchemaElement(char elementId) {
        intArgs.put(elementId, new IntegerArgumentMarshaler());
    }

    private boolean parseArguments() {
        for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
            String arg = args[currentArgument];
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) {
        if (arg.startsWith("-")) {
            parseElements(arg);
        }
    }

    private void parseElements(String arg) {
        for (int i = 1; i < arg.length(); i++) {
            parseElement(arg.charAt(i));
        }
    }

    private void parseElement(char argChar) {
        if (setArgument(argChar)) {
            argsFound.add(argChar);
        } else {
            unexpectedArguments.add(argChar);
        }
    }

    private boolean setArgument(char argChar) {
        boolean set = true;
        if (isBoolean(argChar)) {
            setBooleanArg(argChar, true);
        } else if (isString(argChar)) {
            // the passing parameter "" is never used, we get the real String Arguments by adding argument counter
            // in setStringArg()
            setStringArg(argChar, "");
        } else if (isInt(argChar)) {
            setIntArg(argChar);
        } else set = false;
        return set;
    }

    private boolean isBoolean(char argChar) {
        return booleanArgs.containsKey(argChar);
    }

    private void setBooleanArg(char argChar, boolean value) {
        // NPE? won't happen, has already run isBoolean; But, does this violate the law of Demeter?
        booleanArgs.get(argChar).set("true");
    }

    private boolean isString(char argChar) {
        return stringArgs.containsKey(argChar);
    }

    private void setStringArg(char argChar, String s) {
        currentArgument++;
        try {
            stringArgs.get(argChar).set(args[currentArgument]);
        } catch (ArrayIndexOutOfBoundsException e) {
            valid = false;
            errorArgument = argChar;
            errorCode = ErrorCode.MISSING_STRING;
        }
    }

    private boolean isInt(char argChar) {
        return intArgs.containsKey(argChar);
    }

    private void setIntArg(char argChar) {
        currentArgument++;
        String parameter = null;
        try {
            parameter = args[currentArgument];
            intArgs.get(argChar).setInteger(Integer.parseInt(parameter));
        } catch (ArrayIndexOutOfBoundsException e) {
            valid = false;
            errorArgument = argChar;
            errorCode = ErrorCode.MISSING_INTEGER;
        } catch(NumberFormatException e) {
            valid = false;
            errorArgument = argChar;
            errorParameter = parameter;
            errorCode = ErrorCode.INVALID_INTEGER;
        }
    }

    public int cardinality() {
        return argsFound.size();
    }

    public String usage() {
        if (schema.length() > 0) {
            return "-[" + schema + "]";
        } else {
            return "";
        }
    }

    public String errorMessage() throws Exception {
        if (unexpectedArguments.size() > 0) {
            return unexpetecArgumentMessage();
        } else {
            switch (errorCode) {
                case MISSING_STRING:
                    return String.format("Could not find string parameter for -%c.", errorArgument);
                case OK:
                    throw new Exception("TILT: Should not get here.");
            }
        }
        return "";
    }

    private String unexpetecArgumentMessage() {
        StringBuffer message = new StringBuffer("Argument(s) =");
        for (char c : unexpectedArguments) {
            message.append(c);
        }
        message.append(" unexpected.");
        return message.toString();
    }

    public boolean getBoolean(char arg) {
        ArgumentMarshaler am = booleanArgs.get(arg);
        return am != null && (Boolean)am.get();
    }

    public String getString(char c) {
        ArgumentMarshaler am = stringArgs.get(c);
        return am == null ? "" : (String) am.get();
    }

    public int getInt(char c) {
        ArgumentMarshaler am = intArgs.get(c);
        return am == null ? 0 : am.getInteger();
    }

    private abstract class ArgumentMarshaler {
        private int integerValue;

        public void setInteger(int i) {
            this.integerValue = i;
        }

        public int getInteger() {
            return integerValue;
        }

        public abstract void set(String s);
        public abstract Object get();
    }
    // BooleanArgumentMarshaler is declare private in ArgumentMarshaler in the book, and this is wrong.
    // Because we couldn't call it.
    private class BooleanArgumentMarshaler extends ArgumentMarshaler {
        private boolean booleanValue = false;

        @Override
        public void set(String s) {
            // This is bad, because s is never parsed....?
            booleanValue = true;
        }

        @Override
        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler extends ArgumentMarshaler {
        private String stringValue;

        @Override
        public void set(String s) {
            stringValue = s;
        }

        @Override
        public Object get() {
            return stringValue;
        }
    }

    private class IntegerArgumentMarshaler extends ArgumentMarshaler {
        @Override
        public void set(String s) {

        }

        @Override
        public Object get() {
            return null;
        }
    }
}