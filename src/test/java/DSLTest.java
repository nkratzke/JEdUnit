import static de.thl.jedunit.DSL.CALLABLE;
import static de.thl.jedunit.DSL.CLAZZ;
import static de.thl.jedunit.DSL.CONSTRUCTOR;
import static de.thl.jedunit.DSL.FIELD;
import static de.thl.jedunit.DSL.METHOD;
import static de.thl.jedunit.DSL.b;
import static de.thl.jedunit.DSL.c;
import static de.thl.jedunit.DSL.comment;
import static de.thl.jedunit.DSL.compareClasses;
import static de.thl.jedunit.DSL.d;
import static de.thl.jedunit.DSL.i;
import static de.thl.jedunit.DSL.inspect;
import static de.thl.jedunit.DSL.parse;
import static de.thl.jedunit.DSL.resource;
import static de.thl.jedunit.DSL.s;
import static de.thl.jedunit.DSL.t;
import static de.thl.jedunit.DSL.testWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import org.junit.Test;

import de.thl.jedunit.Selected;
import de.thl.jedunit.SyntaxTree;

public class DSLTest {

    public static int N = 1000;
    
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

    @Test public void testData() {
        assertTrue(
            testWith(
                t(1, "Hello", 3),
                t(1, "Hello", 3),
                t(1, "Hello", 3),
                t(1, "Hello", 3),
                t(1, "Hello", 3)
            ).forAll(t -> t._1 == 1 && t._2.equals("Hello") && t._3 == 3)
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

        compareClasses(reference, submission, t("Reference", "Submission")).results().forEach(r -> {
            r.getPoints();
            if (r.violates()) comment(submission.getFile(), r.getNode().getRange(), r.comment());
        });
        
        inspect(resource("Reference.java.test"), ast -> 
            ast.select(CLAZZ).filter(c -> c.isPublic()).first().isSingle()
        );
    }
}
