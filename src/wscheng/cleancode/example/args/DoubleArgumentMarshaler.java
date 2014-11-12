package wscheng.cleancode.example.args;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoubleArgumentMarshaler implements ArgumentMarshaler {
    private double doubleValue = 0;

    @Override
    public void set(Iterator<String> currentArgument) throws ArgsException {
        String parameter = null;
        try {
            parameter = currentArgument.next();
            doubleValue = Double.parseDouble(parameter);
        } catch (NoSuchElementException e) {
            throw new ArgsException(ArgsException.ErrorCode.MISSING_DOUBLE);
        } catch (NumberFormatException e) {
            throw new ArgsException(ArgsException.ErrorCode.INVALID_DOUBLE, parameter);
        }
    }

    @Override
    public Object get() {
        return doubleValue;
    }

    public static double getValue(ArgumentMarshaler am) {
        try {
            return am == null ? 0 : (Double) am.get();
        } catch (ClassCastException e) {
            return 0;
        }
    }
}
