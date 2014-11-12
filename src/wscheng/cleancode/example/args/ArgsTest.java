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
    }

    public void testTwoStringOneBooleanArguments() throws Exception {
        String testString1 = "my test String1";
        String testString2 = "my test String2";
        Args args = new Args("b*,c*,d", new String[] {"-b", testString1, "-c", testString2, "-d"});
        assertEquals(testString1, args.getString('b'));
        assertEquals(testString2, args.getString('c'));
        assertEquals(true, args.getBoolean('d'));
    }

    public void testTwoStringOneBooleanOneIntegerArguments() throws Exception {
        String testString1 = "my test String1";
        String testString2 = "my test String2";
        String testInt = "10";
        Args args = new Args("b*,c*,d,e#", new String[] {"-b", testString1, "-c", testString2, "-d", "-e", testInt});
        assertEquals(testString1, args.getString('b'));
        assertEquals(testString2, args.getString('c'));
        assertEquals(true, args.getBoolean('d'));
        assertEquals(Integer.parseInt(testInt), args.getInt('e'));
    }

    public void testGetBooleanFromStringArguments() throws Exception {
        String testString1 = "my test String1";
        String testString2 = "my test String2";
        Args args = new Args("b*,c*,d", new String[] {"-b", testString1, "-c", testString2, "-d"});
        assertEquals(testString1, args.getString('b'));
        assertEquals(testString2, args.getString('c'));
        assertEquals(false, args.getBoolean('c'));
    }

    public void testSimpleDoublePresent() throws Exception {
        Args args = new Args("x##", new String[] {"-x", "44.4"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals(44.4, args.getDouble('x'), .001);
    }

    public void testInvalidDouble() throws Exception {
        try {
            new Args("x##", new String[]{"-x", "Forty two"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.INVALID_DOUBLE, e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
            assertEquals("Forty two", e.getErrorParameter());
        }
    }

    public void testMissingDouble() throws Exception {
        try {
            new Args("x##", new String[]{"-x"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.MISSING_DOUBLE, e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
        }
    }

    public void testWithNoSchemaButWithOneArgument() throws Exception {
        try {
            new Args("", new String[]{"-x"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.UNEXPECTED_ARGUMENT,
                    e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
        }
    }
    public void testWithNoSchemaButWithMultipleArguments() throws Exception {
        try {
            new Args("", new String[]{"-x", "-y"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.UNEXPECTED_ARGUMENT,
                    e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
        }
    }
    public void testNonLetterSchema() throws Exception {
        try {
            new Args("*", new String[]{});
            fail("Args constructor should have thrown exception");
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.INVALID_ARGUMENT_NAME,
                    e.getErrorCode());
            assertEquals('*', e.getErrorArgumentId());
        }
    }
    public void testInvalidArgumentFormat() throws Exception {
        try {
            new Args("f~", new String[]{});
            fail("Args constructor should have throws exception");
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.INVALID_FORMAT, e.getErrorCode());
            assertEquals('f', e.getErrorArgumentId());
        }
    }
    public void testSimpleBooleanPresent() throws Exception {
        Args args = new Args("x", new String[]{"-x"});
        assertEquals(1, args.cardinality());
        assertEquals(true, args.getBoolean('x'));
    }
    public void testSimpleStringPresent() throws Exception {
        Args args = new Args("x*", new String[]{"-x", "param"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals("param", args.getString('x'));
    }
    public void testMissingStringArgument() throws Exception {
        try {
            new Args("x*", new String[]{"-x"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.MISSING_STRING, e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
        }
    }
    public void testSpacesInFormat() throws Exception {
        Args args = new Args("x, y", new String[]{"-xy"});
        assertEquals(2, args.cardinality());
        assertTrue(args.has('x'));
        assertTrue(args.has('y'));
    }
    public void testSimpleIntPresent() throws Exception {
        Args args = new Args("x#", new String[]{"-x", "42"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals(42, args.getInt('x'));
    }
    public void testInvalidInteger() throws Exception {
        try {
            new Args("x#", new String[]{"-x", "Forty two"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.INVALID_INTEGER, e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
            assertEquals("Forty two", e.getErrorParameter());
        }
    }
    public void testMissingInteger() throws Exception {
        try {
            new Args("x#", new String[]{"-x"});
            fail();
        } catch (ArgsException e) {
            assertEquals(ArgsException.ErrorCode.MISSING_INTEGER, e.getErrorCode());
            assertEquals('x', e.getErrorArgumentId());
        }
    }

}