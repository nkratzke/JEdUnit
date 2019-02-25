import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.thl.jedunit.Constraints;
import de.thl.jedunit.Evaluator;

class Main {

    public static int countChars(char c, String s) {
        int n = 0;
        for (char x : s.toCharArray()) {
            if (x == c) n++;
        }
        return n;
    }

    /**
     * Here is where everything starts.
     * @param args Command line parameters (not evaluated)
     */
    public static void main(String[] args) {
        System.out.println(countChars('o', "Hello World")); // => 2
        System.out.println(countChars('w', "Hello World")); // => 1
    }
}

class Solution {

    public static int countChars(char c, String s) {
        int n = 0;
        for (char x : s.toLowerCase().toCharArray()) {
            if (x == Character.toLowerCase(c)) n++;
        }
        return n;
    }

}

public class EvaluatorTest {

    public ByteArrayOutputStream system = new ByteArrayOutputStream();
    public final PrintStream redirected = System.out;

    @Before public void tearUp() {
        Evaluator.REALWORLD = false;
        system = new ByteArrayOutputStream();
        System.setOut(new PrintStream(system));
    }

    @After public void tearDown() {
        System.setOut(redirected);
    }

    @Test
    public void testEvaluationProcess() {
        Constraints check = new TestChecks();
        check.runTests(); // process functional tests
        
        String console = system.toString();
        // redirected.println(console);
        
        assertTrue("Commenting", console.contains("<|--") && console.contains("--|>"));
        assertTrue("OK detection", console.contains("[OK] Counting 'o' in \"Hello World\" must return 2."));
        assertTrue("FAILED detection", console.contains("[FAILED] Counting 'w' in \"Hello World\" must return 2."));
        assertTrue("Failing abort condition", console.contains("This kind of abort should not reduce points"));
        assertTrue("Evaluation aborted.", console.contains("Evaluation aborted! This should result in zero points"));
        assertTrue("Handles failing tests", console.contains("failingTest failed"));
        assertTrue("Handles failing checks", console.contains("ArrayIndexOutOfBoundsException"));
        assertTrue("Penalizing messages occur if condition is met", console.contains("[FAILED] This should reduce points (-3% on total result)"));
        assertFalse("Penalizing messages don't occur if condition is not met", console.contains("This message should not occur"));
        assertTrue("Failing penalizing conditions should not reduce points", console.contains("This should not reduce points"));
        assertEquals(1, Stream.of(console.split("\n")).filter(line -> line.endsWith("Grade :=>> 13")).count());
        assertEquals(4, Stream.of(console.split("\n")).filter(line -> line.endsWith("Grade :=>> 0")).count());
        assertEquals(1, Stream.of(console.split("\n")).filter(line -> line.contains("Grade :=>> 12")).count());
        assertEquals(7, Stream.of(console.split("\n")).filter(line -> line.contains("Grade :=>>")).count());
    }
}