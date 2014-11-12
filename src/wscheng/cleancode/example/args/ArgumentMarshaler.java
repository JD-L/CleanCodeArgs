package wscheng.cleancode.example.args;

import java.util.Iterator;

public interface ArgumentMarshaler {
    void set(Iterator<String> currentArgument) throws ArgsException;
    public abstract Object get();
}
