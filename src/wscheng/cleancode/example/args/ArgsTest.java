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

    public void testBooleanStringCardinality() throws Exception {
        Args args = new Args("b,c,d*", new String[] {"-b", "-c", "-d", "test"});
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
        assertEquals(false, args.getBoolean('d'));
    }

    public void testOneStringArguments() throws Exception {
        String testString = "my test String";
        Args args = new Args("b*", new String[] {"-b", testString});
        assertEquals(testString, args.getString('b'));
    }

    public void testTwoStringArguments() throws Exception {
        String testString1 = "my test String1";
        String testString2 = "my test String2";
        Args args = new Args("b*,c*", new String[] {"-b", testString1, "-c", testString2});
        assertEquals(testString1, args.getString('b'));
        assertEquals(testString2, args.getString('c'));
        assertEquals("", args.getString('d'));
    }

    public void testTwoStringOneBooleanArguments() throws Exception {
        String testString1 = "my test String1";
        String testString2 = "my test String2";
        Args args = new Args("b*,c*,d", new String[] {"-b", testString1, "-c", testString2, "-d"});
        assertEquals(testString1, args.getString('b'));
        assertEquals(testString2, args.getString('c'));
        assertEquals(true, args.getBoolean('d'));
    }
}