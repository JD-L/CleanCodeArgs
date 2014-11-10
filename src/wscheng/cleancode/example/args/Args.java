package wscheng.cleancode.example.args;

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    List<String> argsList;
    private boolean valid = true;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private Iterator<String> currentArgument;
    //
    private char errorArgument = '\0';
    private String errorParameter;

    enum ErrorCode {
        OK, MISSING_STRING, INVALID_INTEGER, MISSING_INTEGER, UNEXPECTED_ARGUMENT, MISSING_DOUBLE, INVALID_DOUBLE
    }

    private ErrorCode errorCode = ErrorCode.OK;

    public Args(String schema, String[] args) throws ParseException {
        this.schema = schema;
        this.argsList = Arrays.asList(args);
        valid = parse();
    }

    public boolean isValid() {
        return valid;
    }

    public boolean parse() throws ParseException {
        if (schema.length() == 0 && argsList.size() == 0) {
            return true;
        }
        parseSchema();
        try {
            parseArguments();
        } catch (ArgsException e) {
        }
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

        if (elementTail.length() == 0) {
            marshalers.put(elementId, new BooleanArgumentMarshaler());
        } else if (elementTail.equals("*")) {
            marshalers.put(elementId, new StringArgumentMarshaler());
        } else if (elementTail.equals("#")) {
            marshalers.put(elementId, new IntegerArgumentMarshaler());
        } else if (elementTail.equals("##")) {
            marshalers.put(elementId, new DoubleArgumentMarshaler());
        } else {
            throw new ParseException(String.format("Argument: %c has invalid format: %s", elementId, elementTail), 0);
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

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private boolean isIntSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private boolean parseArguments() throws ArgsException {
        for (currentArgument = argsList.iterator(); currentArgument.hasNext();) {
            String arg = currentArgument.next();
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) throws ArgsException {
        if (arg.startsWith("-")) {
            parseElements(arg);
        }
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1; i < arg.length(); i++) {
            parseElement(arg.charAt(i));
        }
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar)) {
            argsFound.add(argChar);
        } else {
            unexpectedArguments.add(argChar);
            errorCode = ErrorCode.UNEXPECTED_ARGUMENT;
            valid = false;
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        ArgumentMarshaler m = marshalers.get(argChar);
        if (m == null) {
            return false;
        }
        try {
            m.set(currentArgument);
        } catch (ArgsException e) {
            valid = false;
            errorArgument = argChar;
            throw e;
        }
        return true;
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
                case UNEXPECTED_ARGUMENT:
                    return unexpetecArgumentMessage();
                case MISSING_STRING:
                    return String.format("Could not find string parameter for -%c.", errorArgument);
                case INVALID_INTEGER:
                    return String.format("Argument -%c expects an integer but was '%s'.", errorArgument, errorParameter);
                case MISSING_INTEGER:
                    return String.format("Could not find integer parameter for -%c.", errorArgument);
                case INVALID_DOUBLE:
                    return String.format("Argument -%c expects a double but was '%s'.", errorArgument, errorParameter);
                case MISSING_DOUBLE:
                    return String.format("Could not find double parameter for -%c.", errorArgument);
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
        ArgumentMarshaler am = marshalers.get(arg);
        try {
            return am != null && (Boolean) am.get();
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String getString(char c) {
        ArgumentMarshaler am = marshalers.get(c);
        try {
            return am == null ? "" : (String) am.get();
        } catch (ClassCastException e) {
            return "";
        }
    }

    public int getInt(char c) {
        ArgumentMarshaler am = marshalers.get(c);
        try {
            return am == null ? 0 : (Integer) am.get();
        } catch (ClassCastException e) {
            return 0;
        }
    }

    public double getDouble(char c) {
        ArgumentMarshaler am = marshalers.get(c);
        try {
            return am == null ? 0 : (Double) am.get();
        } catch (ClassCastException e) {
            return 0;
        }
    }

    public boolean has(char c) {
        return argsFound.contains(c);
    }

    private interface ArgumentMarshaler {
        public abstract void set(Iterator<String> currentArgument) throws ArgsException;
        public abstract Object get();
    }
    // BooleanArgumentMarshaler is declare private in ArgumentMarshaler in the book, and this is wrong.
    // Because we couldn't call it.
    private class BooleanArgumentMarshaler implements ArgumentMarshaler {
        private boolean booleanValue = false;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            booleanValue = true;
        }

        @Override
        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler implements ArgumentMarshaler {
        private String stringValue;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            try {
                stringValue = currentArgument.next();
            } catch (NoSuchElementException e) {
                errorCode = ErrorCode.MISSING_STRING;
                throw new ArgsException();
            }
        }

        @Override
        public Object get() {
            return stringValue;
        }
    }

    private class IntegerArgumentMarshaler implements ArgumentMarshaler {
        private int integerValue = 0;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                integerValue = Integer.parseInt(parameter);
            } catch (NoSuchElementException e) {
                errorCode = ErrorCode.MISSING_INTEGER;
                throw new ArgsException();
            } catch (NumberFormatException e) {
                errorParameter = parameter;
                errorCode = ErrorCode.INVALID_INTEGER;
                throw new ArgsException();
            }
        }

        @Override
        public Object get() {
            return integerValue;
        }
    }

    private class DoubleArgumentMarshaler implements ArgumentMarshaler {
        private double doubleValue = 0;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                doubleValue = Double.parseDouble(parameter);
            } catch (NoSuchElementException e) {
                errorCode = ErrorCode.MISSING_DOUBLE;
                throw new ArgsException();
            } catch (NumberFormatException e) {
                errorParameter = parameter;
                errorCode = ErrorCode.INVALID_DOUBLE;
                throw new ArgsException();
            }
        }

        @Override
        public Object get() {
            return doubleValue;
        }
    }

    private class ArgsException extends Exception {

    }
}