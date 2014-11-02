package wscheng.cleancode.example.args;

import junit.framework.TestCase;

public class ArgsTest extends TestCase {
    public void testCreateWithNoSchemaOrArguments() throws Exception {
        Args args = new Args("", new String[0]);
        assertEquals(0, args.cardinality());
    }

    public void testCardinality() throws Exception {
        Args args = new Args("b,c,d", new String[] {"-b", "-c", "-d"});
        assertEquals(3, args.cardinality());
    }

    public void testOneBooleanArguments() throws Exception {
        Args args = new Args("b", new String[] {"-b"});
        assertEquals(true, args.getBoolean('b'));
    }

    public void testTwoBooleanArguments() throws Exception {
        Args args = new Args("b,c", new String[] {"-b", "-c"});
        assertEquals(true, args.getBoolean('b'));
        assertEquals(true, args.getBoolean('c'));
        // will get NPE
        // assertEquals(false, args.getBoolean('d'));
    }

}