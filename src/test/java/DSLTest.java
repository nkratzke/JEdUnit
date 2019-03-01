import static de.thl.jedunit.DSL.CALLABLE;
import static de.thl.jedunit.DSL.CLAZZ;
import static de.thl.jedunit.DSL.CONSTRUCTOR;
import static de.thl.jedunit.DSL.FIELD;
import static de.thl.jedunit.DSL.METHOD;
import static de.thl.jedunit.DSL.b;
import static de.thl.jedunit.DSL.c;
import static de.thl.jedunit.DSL.d;
import static de.thl.jedunit.DSL.f;
import static de.thl.jedunit.DSL.i;
import static de.thl.jedunit.DSL.inspect;
import static de.thl.jedunit.DSL.l;
import static de.thl.jedunit.DSL.parse;
import static de.thl.jedunit.DSL.repr;
import static de.thl.jedunit.DSL.resource;
import static de.thl.jedunit.DSL.s;
import static de.thl.jedunit.DSL.t;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.function.Supplier;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.Test;

import de.thl.jedunit.CompareResult;
import de.thl.jedunit.Constraints;
import de.thl.jedunit.DSL;
import de.thl.jedunit.Evaluator;
import de.thl.jedunit.Selected;
import de.thl.jedunit.SyntaxTree;

public class DSLTest extends Constraints {

    public static int N = 1000;
    
    @Test public void testComment() {
        DSL.comment(repr("This\nis\njust\nan\nexample"));
        DSL.comment(repr("This\tis\ta\ttest"));
    }

    @Test public void testGenerateString() {
        Stream.iterate(0, l -> l + 1).limit(N / 100).forEach(l -> {
            String generated = s(String.format("[a-cA-C]{0,%d}", l));
            assertTrue("Generated string should be longer than 1.", generated.length() >= 0);
            assertTrue("Generated string should be shorter than 7.", generated.length() <= l);
            assertTrue("Generated string should not contain a 'D'.", !generated.contains("D"));
            assertTrue("Generated string should not contain a 'd'.", !generated.contains("d"));

            generated = s(0, l);
            assertTrue("Generated string should be longer than " + l, generated.length() <= l);
            assertFalse("Generated string should only contain [a-zA-Z]", generated.contains("ü"));
        });
    }

    @Test public void testGenerateBool() {
        Stream.generate(() -> 1).limit(N).forEach(t -> {
            boolean generated = b();
            assertTrue("Generated boolean should return true or false.", generated == true || generated == false);
        });
    }

    @Test public void testGenerateInteger() {
        Stream.generate(() -> 1).limit(N).forEach(t -> {
            int i = i();
            assertTrue("Generated integer should work.", Integer.MIN_VALUE <= i && i <= Integer.MAX_VALUE);
            i = i(10);
            assertTrue("Generated integer should be between 0 and upper bound.", i >= 0 && i < 10);
            i = i(-10, 10);
            assertTrue("Generated integer should be in specified borders.", i >= -10 && i < 10);
        });
    }

    @Test public void testGenerateDouble() {
        Stream.iterate(1.0, d -> d + 1.0).limit(N).forEach(d -> {
            double r = d();
            assertTrue("Generate double should work.", -Double.MAX_VALUE < r && r < Double.MAX_VALUE);
            r = d(d);
            assertTrue("Generated double should be between 0 and upper bound.", r >= 0.0 && r < d);
            r = d(-d, d);
            assertTrue("Generated double should be in specified borders.", r >= -d && r < d);
        });
    }

    @Test public void testGenerateChar() {
        Stream.generate(() -> true).limit(N).forEach(b -> {
            char c = c("[a-z]");
            assertTrue("Generated char should be in [a-z]", c >= 'a' && c <= 'z');
            c = c();
            assertTrue("Generated char should be in [a-z]", c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
        });
    }

    @Test public void testGenerateList() {
        Stream.generate(() -> true).limit(N).forEach(list -> {
            int length = i(3, 10);
            java.util.List<String> l = l(3, length, () -> s(1, 10));
            assertTrue(l.size() >= 3);
            assertTrue(l.size() <= 10);
        });

        assertEquals(3, l(3, () -> s(1, 10)).size());
        assertEquals(2, l(2, () -> s(1, 10)).size());
        assertEquals(1, l(1, () -> s(1, 10)).size());
    }

    @Test public void testTupleBuilding() {
        int a = 42;
        boolean b = false;
        char c = 'X';
        double d = Math.PI;
        String e = "Test";
        float f = 1.0f;
        byte g = 0;
        String[] h = { "Hello", "World" };
        
        assertTrue(a == t(a, b)._1);
        assertTrue(b == t(a, b)._2);

        assertFalse(t(b, c, d)._1);
        assertTrue(c == t(b, c, d)._2);
        assertTrue(d == t(b, c, d)._3);

        assertTrue(b == t(b, c, d, e)._1);
        assertTrue(c == t(b, c, d, e)._2);
        assertTrue(d == t(b, c, d, e)._3);
        assertEquals(e, t(b, c, d, e)._4);

        assertTrue(a == t(a, b, c, d, e)._1);
        assertTrue(b == t(a, b, c, d, e)._2);
        assertTrue(c == t(a, b, c, d, e)._3);
        assertTrue(d == t(a, b, c, d, e)._4);
        assertEquals(e, t(a, b, c, d, e)._5);

        assertTrue(f == t(a, b, c, d, e, f)._6);
        assertTrue(g == t(a, b, c, d, e, f, g)._7);
        assertArrayEquals(h, t(a, b, c, d, e, f, g, h)._8);
    }

    @Test public void testRepr() {
        assertEquals("\"\u23b5\u21b9\u23ce\n\"", repr(" \t\n"));
        assertEquals("\"\"", repr(""));
        assertEquals("'x'", repr('x'));
        assertEquals("'\u23b5'", repr(' '));
        assertEquals("'\u21b9'", repr('\t'));
        assertEquals("'\u23ce'", repr('\n'));
        assertEquals("null", repr("test".length() > "test".length() ? "test" : null));
    }

    @Test public void testListRepr() {
        java.util.List<String> strings = Arrays.asList("", " ", "Hello World");
        java.util.List<Integer> ints = Arrays.asList(1, 2, 3);
        assertEquals(String.format("[%s, %s, %s]", repr(""), repr(" "), repr("Hello World")), repr(strings));
        assertEquals(String.format("[%d, %d, %d]", 1, 2, 3), repr(ints));
    }

    @Test public void testFormat() {
        assertEquals("[1, 2, 3, 4]", f("%s", Arrays.asList(1, 2, 3, 4)));
        assertEquals("[" + repr("This is just a test") + "]", f("%s", Arrays.asList("This is just a test")));
        assertEquals("1 true 'C' \"\u21b9\"", f("%s %s %s %s", 1, true, 'C', "\t"));
    }

    @Test public void testTest() {
        test(
            t(1, "Hello", 3),
            t(1, "Hello this is just a  test", 3),
            t(1, "Hello", 3)        
        ).each(
            d -> d._3,
            d -> (d._1 + d._2).equals("" + d._1 + d._1),
            d -> String.format("call(%d, %s) should return %s", d._1, repr(d._2), repr(d._1 + d._2)),
            d -> String.format("but returned " + repr("" + d._1 + d._1))
        );

        test(
            t(1, "Hello"),
            t(1, "Hello this is just a  test"),
            t(1, "Hello")
        ).each(
            d -> (d._1 + d._2).equals("" + d._1 + d._1),
            d -> String.format("call(%d, %s) should return %s", d._1, repr(d._2), repr(d._1 + d._2))
        );
    }

    @Test public void testParse() {
        SyntaxTree ast = parse(resource("Main.java.template"));
        assertEquals(1, ast.select(CLAZZ).count());
        assertEquals(0, ast.select(FIELD).count());
        assertEquals(0, ast.select(CONSTRUCTOR).count());
        assertEquals(2, ast.select(METHOD).count());
    }

    @Test
    public void testNonSuccessfulParse() {
        assertNull(parse("NotExisting.java"));
        assertNull(parse(resource("SyntaxError.java.test")));
    }

    @Test
    public void testInspect() {
        assertTrue(inspect(resource("Reference.java.test"), ast -> ast.select(CLAZZ).isSingle()));
        assertFalse(inspect(resource("SyntaxError.java.test"), ast -> true));
        assertFalse(inspect("NotExisting.java", ast -> true));
    }

    @Test
    public void testCompare() throws Exception {
        Selected<ClassOrInterfaceDeclaration> submission = parse(resource("Submission.java.test")).select(CLAZZ).first();
        Selected<ClassOrInterfaceDeclaration> reference = parse(resource("Reference.java.test")).select(CLAZZ).first();
        Selected<ClassOrInterfaceDeclaration> multiples = parse(resource("Multiple.java.test")).select(CLAZZ);
        Selected<ClassOrInterfaceDeclaration> empty = parse(resource("Multiple.java.test")).select(CLAZZ).filter(c -> c.isPublic());

        assertNull(compareClasses(reference, multiples));
        assertNull(compareClasses(multiples, reference));
        assertNull(compareClasses(multiples, multiples));

        assertTrue(compareClasses(submission, submission).noViolations());
        assertTrue(compareClasses(reference, reference).noViolations());

        int members = submission.select(FIELD).count() + submission.select(CALLABLE).count();
        assertEquals(0, compareClasses(empty, submission, t("Multiple", "Submission")).violations().count());
        assertEquals(members, compareClasses(submission, empty, t("Submission", "Multiple")).violations().count());
        assertEquals(members + 1, compareClasses(submission, empty, t("Stupid", "Nonsense")).violations().count());

        assertEquals(3, compareClasses(reference, submission, t("Reference", "Submission")).violations().count());
        assertEquals(3, compareClasses(reference, submission, t("Stupid", "Nonsense"), t("Reference", "Submission")).violations().count());        
    }

    public void testCompareClassesOutput() {
        Evaluator.LOG.clear();

        Selected<ClassOrInterfaceDeclaration> submission = parse(resource("Submission.java.test")).select(CLAZZ).first();
        Selected<ClassOrInterfaceDeclaration> reference = parse(resource("Reference.java.test")).select(CLAZZ).first();
        CompareResult result = compareClasses(reference, submission, t("Reference", "Submission"));

        report(result, submission);
        String console = report();

        assertTrue(console.contains("[OK] Class declaration correct (1 points)"));
        assertTrue(console.contains("[OK] Datafield found: public int datafield (1 points)"));
        assertTrue(console.contains("[OK] Datafield found: public static String CONST (2 points)"));
        assertTrue(console.contains("[OK] Method found: public Submission method(Submission) (3 points)"));
        assertTrue(console.contains("[OK] Datafield found: protected List<Submission> next (5 points)"));
        assertTrue(console.contains("[FAILED] Missing/wrong declared datafield:  String notSubmitted (0 of 1 points)"));
        assertTrue(console.contains("[FAILED] Missing/wrong declared datafield: public int other (0 of 1 points)"));
        assertTrue(console.contains("[FAILED] Missing/wrong declared method: protected boolean notFound() (0 of 1 points)"));
    }

    @Test public void testAssertEqualsGeneral() {
        assertTrue(DSL.assertEquals(5, 2 + 3));
        assertTrue(DSL.assertEquals(DSL.l(5, () -> 1), Arrays.asList(1, 1, 1, 1, 1)));
        assertTrue(DSL.assertEquals(DSL.l(0, () -> 1), Arrays.asList()));
        assertFalse(DSL.assertEquals(null, "Something"));
        assertFalse(DSL.assertEquals("Something", null));
        assertTrue(DSL.assertEquals(null, null));
    }

    @Test public void testAssertEqualsMap() {
        Map<String, Integer> a = new HashMap<>();
        Map<String, Integer> b = new TreeMap<>();

        assertTrue(DSL.assertEquals(a, b));
        assertTrue(DSL.assertEquals(b, a));
        assertTrue(DSL.assertEquals(a, a));
        assertTrue(DSL.assertEquals(b, b));

        a.put("", 0);
        b.put("", 1);

        assertFalse(DSL.assertEquals(a, b));
        assertFalse(DSL.assertEquals(b, a));
        assertTrue(DSL.assertEquals(a, a));
        assertTrue(DSL.assertEquals(b, b));

        a.clear();
        b.clear();
        for (int i = 0; i < 100; i++) {
            a.put("Key: " + (99 - i), 99 - i);
            b.put("Key: " + i, i);
        }

        assertFalse(a.toString().equals(b.toString()));
        assertTrue(DSL.assertEquals(a, b));
        assertTrue(DSL.assertEquals(b, a));
        assertTrue(DSL.assertEquals(a, a));
        assertTrue(DSL.assertEquals(b, b));

        a.put("42", -1);
        assertFalse(DSL.assertEquals(b, a));
        assertFalse(DSL.assertEquals(a, b));
        assertTrue(DSL.assertEquals(a, a));
        assertTrue(DSL.assertEquals(b, b));

    }

    @Test public void testCaptureException() {

        Supplier<?> raisingCode = () -> {
            String nullPointer = "Hello".length() > 0 ? null : "";
            return nullPointer.length();
        };

        Supplier<?> workingCode = () -> {
            return "Hello World".length();
        };

        assertEquals("java.lang.NullPointerException", DSL.captureException(raisingCode));
        assertNull(DSL.captureException(workingCode));
    }
}
