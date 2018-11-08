package de.thl.jedunit;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.javaparser.Range;

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
     * Version (Semantic Versioning).
     */
    public static final String VERSION = "0.1.11";

    /**
     * The maximum points for a VPL assignment.
     */
    private static final int MAX = 100;

    /**
     * The currently reached points for a VPL assignment.
     */
    private int points = 0;

    /**
     * Test case counter.
     * Declared static to count testcases consecutively across
     * different Check classes.
     */
    private static int testcase = 0;

    /**
     * Current points (truncated to [0, 100])
     * @return points [0, 100]
     */
    public int getPoints() {
        int report = this.points;
        report = report > MAX ? MAX : report;
        report = report < 0 ? 0 : report;
        return report;
    }

    /**
     * Adds points for grading if a check is passed (wishful behavior).
     * A comment is always printed whether the check was successfull or not.
     * @param add Points to add (on success)
     * @param remark Comment to show
     * @param check Condition to check (success)
     */
    protected final void grading(int add, String remark, Supplier<Boolean> check) {
        testcase++;
        try {
            if (check.get()) {
                this.points += add;
                comment("Check " + testcase + ": [OK] " + remark + " (" + add + " points)");
            } else comment("Check " + testcase + ": [FAILED] " + remark + " (0 of " + add + " points)");
        } catch (Exception ex) {
            comment("Check " + testcase + ": [FAILED due to " + ex + "] " + remark + " (0 of " + add + " points)");
        }
    }

    /**
     * Deletes points (penalzing) if a check is passed (unwishful behavior).
     * A comment is only printed if the check indicates a violation.
     * @param penalty Points to remove (on violation)
     * @param remark Comment to show
     * @param violation Violation condition to check
     */
    protected final void penalize(int penalty, String remark, Supplier<Boolean> violation) {
        try {
            if (!violation.get()) return;
            this.points -= penalty;
            comment("[FAILED] " + remark + " (-" + penalty + " points)");
        } catch (Exception ex) {
            comment("[FAILED due to " + ex + "] " + remark);
        }
    }

    /**
     * Checks whether a severe condition is met. E.g. a cheating submission.
     * In case the check is evaluated to true the evaluation is aborted immediately.
     */
    protected final void abortOn(String comment, Supplier<Boolean> violation) {
        try {
            if (!violation.get()) return;
            comment("Evaluation aborted! " + comment);
            this.points = 0;
            if (REALWORLD) {
                grade();
                System.exit(1);
            }
        } catch (Exception ex) {
            comment("[FAILED due to " + ex + "] " + comment);
        }
    }

    /**
     * Can be used to formulate arbitrary AST-based checks on parsed source code.
     * @param file File to load an parse
     * @param test Logical predicate on the AST of file
     * @return evaluated test predicate
     *         false in case the file could not be loaded or parsed
     */
    protected final boolean check(String file, Predicate<SyntaxTree> test) {
        try {
            return test.test(new SyntaxTree(file));
        } catch (Exception ex) {
            comment("Check failed: " + ex);
            comment("Is there a syntax error in your submission? " + file);
            return false;
        }
    }

    /**
     * Adds a comment for VPL via console output.
     */
    public static void comment(String c) { 
        System.out.println("Comment :=>> " + c); 
    }

    /**
     * Adds a comment for VPL via console output.
     * Marks file and position via a JavaParser range.
     */
    public static void comment(String file, Optional<Range> range, String c) {
        if (!range.isPresent()) comment(file + ": " + c);
        int line = range.get().begin.line;
        int col = range.get().begin.column;
        comment(String.format("%s:%d:%d: %s", file, line, col, c));
    }

    /**
     * Reports the current points to VPL via console output (truncated to [0, 100]).
     */
    public void grade() {
        System.out.println("Grade :=>> " + this.getPoints());
    }

    private List<Method> allMethodsOf(Class<?> clazz) {
        if (clazz == null) return new LinkedList<>();
        List<Method> methods = allMethodsOf(clazz.getSuperclass());
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    /**
     * Executes all methods annoted with a specified annotation.
     * Methods are executed according to their alphabetical order.
     * @param annotation Annotation, one of [@Constraint, @Check]
     */
    public final void process(Class<? extends Annotation> annotation) {
        allMethodsOf(this.getClass())
            .stream()
            .filter(method -> method.isAnnotationPresent(annotation))
            .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
            .forEach(method -> {
                try {
                    method.invoke(this);
                    comment("");
                    grade();
                } catch (Exception ex) {
                    comment("Test method " + method.getName() + " failed completely." + ex);
                    grade();
                }    
            });
    }

    /**
     * Indicates whether JEdUnit runs in the real world (true)
     * or under unit test conditions (false)
     */
    public static boolean REALWORLD = true;

    /**
     * This method evaluates the checkstyle log file.
     */
    public final void checkstyle() {
        try {
            comment("Checkstyle");
            Scanner in = new Scanner(new File("checkstyle.log"));
            while (in.hasNextLine()) {
                String result = in.nextLine();
                for (String file : Config.EVALUATED_FILES) {
                    if (!result.contains(file)) continue;
                    if (Config.CHECKSTYLE_IGNORES.stream().anyMatch(ignore -> result.contains(ignore))) continue;

                    String msg = result.substring(result.indexOf(file));
                    comment(msg);
                    this.points -= Config.CHECKSTYLE_PENALTY;
                }
            }
            in.close();
            if (this.points >= 0) comment("Everything fine");
            if (this.points < 0) comment("[CHECKSTYLE] Found violations: (" + this.points + " points)");
        } catch (Exception ex) {
            comment("You are so lucky! We had problems processing the checkstyle.log.");
            comment("This was due to: " + ex);
        }
    }

    /**
     * Runs the evaluation.
     * @param args command line options (not evaluated)
     */
    public static final void main(String[] args) {
        try {
            Constraints check = (Constraints)Class.forName("Checks").getDeclaredConstructor().newInstance();
            comment("JEdUnit " + Evaluator.VERSION);
            comment("");
            check.configure();
            if (Config.CHECKSTYLE) check.checkstyle();
            comment("");
            check.process(Constraint.class); // process restriction checks
            check.process(Check.class);      // process functional tests
            comment(String.format("Finished: %d points", check.getPoints()));
        } catch (Exception ex) {
            comment("Severe error: " + ex);
        }
    }
}