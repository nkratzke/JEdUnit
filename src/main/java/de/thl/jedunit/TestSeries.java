package de.thl.jedunit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A TestData object to execute a series of tests.
 * @author Nane Kratzke
 * @since 0.2.0
 */
public class TestSeries<T> {
    
    private List<T> data = new LinkedList<>();
    
    private Evaluator evaluator;

    /**
     * Constructor to create data for a series of tests.
     */
    @SafeVarargs
    public TestSeries(Evaluator ev, T... data) {
        this.evaluator = ev;
        this.data = Arrays.asList(data);
    }

    /**
     * Checks each test data with the following set of functions.
     * @param points Lambda function to calculate the points for this testcase.
     * @param matches Predicate to check wether expected results matches the actual result.
     * @param expected Lambda function to report the call and expected outcome.
     * @param actual Lambda function to report the actual outcome (in case of a none match)
     */
    public void each(
        Function<T, Integer> points,
        Predicate<T> matches,
        Function<T, String> expected,
        Function<T, String> actual
    ) {
        for (T d : this.data) {
            try {
                String expectedMsg = expected.apply(d);
                int p = points.apply(d);
                try {
                    boolean success = matches.test(d);
                    if (success) {
                        this.evaluator.grading(p, expectedMsg, () -> success);
                    } else {
                        String failedMsg = String.format("%s %s", expectedMsg, actual.apply(d));
                        this.evaluator.grading(p, failedMsg, () -> success);
                    }
                } catch (Exception ex) {
                    String comment = String.format("%s but failed with exception: %s", expectedMsg, ex);
                    this.evaluator.grading(p, comment, () -> false);
                }
            } catch (Exception ex) {
                String comment = String.format("Test error: test data %s failed with exception: %s", d, ex);
                this.evaluator.grading(0, comment, () -> false);
            }    
        }
    }

    /**
     * Checks each test data with the following set of functions.
     * @param points Constant points for each testcase.
     * @param matches Predicate to check wether expected results matches the actual result.
     * @param expected Lambda function to report the call and expected outcome.
     * @param actual Lambda function to report the actual outcome (in case of a none match)
     */
    public void each(
        int points,
        Predicate<T> matches,
        Function<T, String> expected,
        Function<T, String> actual
    ) { this.each(__ -> points, matches, expected, actual); }

    /**
     * Checks each test data with the following set of functions.
     * @param matches Predicate to check wether expected results matches the actual result.
     * @param expected Lambda function to report the call and expected outcome.
     * @param actual Lambda function to report the actual outcome (in case of a none match)
     */
    public void each(
        Predicate<T> matches,
        Function<T, String> expected,
        Function<T, String> actual
    ) { this.each(__ -> 5, matches, expected, actual); }

    /**
     * Checks each test data with the following set of functions.
     * @param matches Predicate to check wether expected results matches the actual result.
     * @param expected Lambda function to report the call and expected outcome.
     */
    public void each(
        Predicate<T> matches,
        Function<T, String> expected
    ) { this.each(__ -> 5, matches, expected, __ -> "but didn't."); }
}