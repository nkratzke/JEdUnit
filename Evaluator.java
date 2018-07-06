import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Basic evaluator for automatic evaluation of programming excercise assignments.
 * This evaluator is intended to be used with Moodle and VPL (Virtual Programming Lab).
 * It provides basic capabilities to evaluate programming assignents.
 * 
 * !!! Normally there is no need to touch this file !!!
 * !!! Keep it, unless you are perfectly knowing what you are doing !!!
 * 
 * @author Nane Kratzke
 */
public class Evaluator {

    /**
     * The maximum points for a VPL assignment.
     */
    private int MAX = 100;

    /**
     * The currently reached points for a VPL assignment.
     */
    private int points = 0;

    /**
     * Test case counter.
     */
    private int testcase = 0;

    /**
     * Adds a percentage to the maximum reachable points (grading)
     * if a check is passed. Otherwise a remark will be printed.
     * The reached points can be evaluated by VPL.
     */
    protected final void grading(int add, String remark, Supplier<Boolean> check) {
        try {
            if (check.get()) {
                points += add;
                points = points > MAX ? MAX : points;
                points = points < 0 ? 0 : points;
                System.out.println(comment("Testcase " + ++testcase + ": " + remark + " [OK]"));
                System.out.println("Grade :=>> " + points); 
            } else System.out.println(comment(remark + " [FAILED]"));
        } catch (Exception ex) {
            System.out.println(comment(remark + " [FAILED due to " + ex + "]"));
        }
    }

    /**
     * Adds a VPL comment.
     */
    protected final String comment(String c) { return "Comment :=>> " + c; }

    /**
     * This method scans and invokes all methods starting with "test" to run the grading.
     */
    protected final void evaluate() {
        for (Method test : this.getClass().getDeclaredMethods()) {
            if (!test.getName().startsWith("test")) continue;
            try {
                test.invoke(this);
            } catch (Exception ex) {
                System.out.println("Test case " + test.getName() + " failed completely." + ex);
            }
        }
    }

    /**
     * The main method calls the evaluation.
     */
    public static final void main(String[] args) {
        Checks checks = new Checks();
        checks.evaluate();
    }
}