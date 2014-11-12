package wscheng.cleancode.example.args;

public class ArgsException extends Exception {
    private char errorArgument = '\0';
    private String errorParameter;
    private ErrorCode errorCode = ErrorCode.OK;
    enum ErrorCode {
        OK, MISSING_STRING, INVALID_INTEGER, MISSING_INTEGER, UNEXPECTED_ARGUMENT, MISSING_DOUBLE, INVALID_DOUBLE
    }

    ArgsException() {

    }

    ArgsException(String message) {
        super(message);
    }
}
