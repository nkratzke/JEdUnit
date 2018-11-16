package de.thl.jedunit;

import static de.thl.jedunit.DSL.comment;
import static de.thl.jedunit.DSL.t;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import io.vavr.Tuple2;

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
    // public static final String VERSION = "0.1.16";

    /**
     * The maximum points for a VPL assignment.
     */
    private static final int MAX = 100;

    /**
     * The currently reached percentage of maximum points.
     */
    private double percentage = 0.0;

    /**
     * The results of the latest executed testcase.
     */
    private final List<Tuple2<Integer, Integer>> results = new LinkedList<>();

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
        int report = (int)Math.round(this.percentage * MAX);
        report = report > MAX ? MAX : report;
        report = report < 0 ? 0 : report;
        return report;
    }

    /**
     * Adds points for grading if a check is passed (wishful behavior).
     * A comment is always printed whether the check was successfull or not.
     * @param p Points to add (on success)
     * @param comment Comment to show
     * @param check Condition to check (success)
     */
    protected final void grading(int p, String comment, Supplier<Boolean> check) {
        testcase++;
        try {
            if (check.get()) {
                results.add(t(p, p));
                comment("Check " + testcase + ": [OK] " + comment + " (" + p + " points)");
            } else {
                results.add(t(0, p));
                comment("Check " + testcase + ": [FAILED] " + comment + " (0 of " + p + " points)");
            }
        } catch (Exception ex) {
            results.add(t(0, p));
            comment("Check " + testcase + ": [FAILED due to " + ex + "] " + comment + " (0 of " + p + " points)");
        }
    }

    /**
     * Deletes percentage points (penalzing) if a check is passed (unwishful behavior).
     * A comment is only printed if the check indicates a violation.
     * Penalities are applied to the complete percentage even if launched from 
     * weighted checks.
     * @param penalty Percentage points to remove (on violation)
     * @param remark Comment to show
     * @param violation Violation condition to check
     */
    protected final void penalize(int penalty, String remark, Supplier<Boolean> violation) {
        try {
            if (!violation.get()) return;
            this.percentage -= penalty / 100.0;
            comment(String.format("[FAILED] %s (-%d%% on total result)", remark, penalty));
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
            this.percentage = 0;
            if (REALWORLD) {
                grade();
                System.exit(1);
            }
        } catch (Exception ex) {
            comment("[FAILED due to " + ex + "] " + comment);
        }
    }

    /**
     * Reports the current points to VPL via console output (truncated to [0, 100]).
     */
    public void grade() {
        comment(String.format("Current percentage: %.0f%%", this.percentage * 100));
        System.out.println("Grade :=>> " + this.getPoints());
    }

    /**
     * Reports current results to VPL via console output (truncated to [0, 100]).
     * @param results List of results [(n, of possible points)]
     * @param weight Weight to be considered for total sum
     */
    public void grade(double weight, List<Tuple2<Integer, Integer>> results) {
        if (results.isEmpty() || weight <= 0.0) {
            comment("No results or weight for this test");
            grade();
            return;
        }
        int points = results.stream().map(d -> d._1).reduce(0, (a, b) -> a + b);
        int total = results.stream().map(d -> d._2).reduce(0, (a, b) -> a + b);
        double p = 100.0 * points / total;
        comment(String.format("Result for this test: %d of %d points (%.0f%%)", points, total, p));
        this.percentage += (weight * points) / total;
        grade();
    }

    private List<Method> allMethodsOf(Class<?> clazz) {
        if (clazz == null) return new LinkedList<>();
        List<Method> methods = allMethodsOf(clazz.getSuperclass());
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    /**
     * Executes all methods annoted with a Test annotation.
     * Methods are executed according to their alphabetical order.
     */
    public final void runTests() {
        allMethodsOf(this.getClass())
            .stream()
            .filter(method -> method.isAnnotationPresent(Test.class))
            .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
            .forEach(method -> {
                try {
                    Test t = method.getAnnotation(Test.class);
                    comment(String.format("- [%.2f%%]: ", t.weight() * 100) + t.description());
                    results.clear();
                    method.invoke(this);
                    grade(t.weight(), results);
                    comment("");
                } catch (Exception ex) {
                    comment("Test " + method.getName() + " failed completely." + ex);
                    grade();
                }
                results.clear();
            });
    }

    /**
     * Executes all methods annoted with a Test annotation.
     * Methods are executed according to their alphabetical order.
     */
    public final void runInspections() {
        allMethodsOf(this.getClass())
            .stream()
            .filter(method -> method.isAnnotationPresent(Inspection.class))
            .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
            .forEach(method -> {
                try {
                    results.clear();
                    Inspection i = method.getAnnotation(Inspection.class);
                    comment("- " + i.description());
                    method.invoke(this);
                    grade();
                    comment("");
                } catch (Exception ex) {
                    comment("Inspection " + method.getName() + " failed completely." + ex);
                    grade();
                }
                results.clear();
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
            comment("- Checkstyle");
            Scanner in = new Scanner(new File("checkstyle.log"));
            while (in.hasNextLine()) {
                String result = in.nextLine();
                for (String file : Config.EVALUATED_FILES) {
                    if (!result.contains(file)) continue;
                    if (Config.CHECKSTYLE_IGNORES.stream().anyMatch(ignore -> result.contains(ignore))) continue;

                    String msg = result.substring(result.indexOf(file));
                    comment(msg);
                    this.percentage -= Config.CHECKSTYLE_PENALTY / 100.0;
                }
            }
            in.close();
            if (this.percentage >= 0) comment("Everything fine");
            if (this.percentage < 0) {
                String msg = String.format("[CHECKSTYLE] Found violations (%d%%)", (int)(this.percentage * 100));
                comment(msg);
            }
            grade();
        } catch (Exception ex) {
            comment("You are so lucky! We had problems processing the checkstyle.log.");
            comment("This was due to: " + ex);
            grade();
        }
    }

    /**
     * Runs the evaluation.
     * @param args command line options (not evaluated)
     */
    public static final void main(String[] args) {
        try {
            Constraints check = (Constraints)Class.forName("Checks").getDeclaredConstructor().newInstance();
            comment("JEdUnit " + Config.VERSION);
            comment("");
            check.configure();
            if (Config.CHECKSTYLE) check.checkstyle();
            comment("");
            check.runInspections();
            check.runTests();
            comment(String.format("Finished: %d points", check.getPoints()));
        } catch (Exception ex) {
            comment("Severe error: " + ex);
        }
    }
}