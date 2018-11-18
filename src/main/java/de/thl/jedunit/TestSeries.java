package de.thl.jedunit;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
     * @param points Lambda function to calculate the points.
     * @param expected Lambda function to determine the expected result.
     * @param result Lambda function to determine the result of the logic under test.
     * @param matches Lambda predicate to determine whether result matches expected outcome.
     * @param call Lambda function to explain the call and its intended result.
     * @param wrong In case of a none match, the lamdba function to report the wrong outcome of the test.
     */
    public <R> void each(
        Function<T, Integer> points,
        Function<T, R> expected, 
        Function<T, R> result,
        BiPredicate<R, R> matches,
        Function<T, String> call,
        Function<T, String> wrong
    ) {
        for (T d : this.data) {
            try {
                R e, r;
                int p = points.apply(d);
                e = expected.apply(d);
                String call = msg.apply(d);
                try { r = result.apply(d); } 
                catch (Exception ex) {
                    String comment = String.format("%s but failed with exception: %s", call, ex);
                    this.evaluator.grading(p, comment, () -> false);
                    continue;
                }
                boolean ok = matches.test(e, r);
                String error = ok ? "" : " " + wrong.apply(d);
                this.evaluator.grading(p, call + error, () -> ok);
            } catch (Exception ex) {
                String comment = String.format("Test error: test data %s failed with exception: %s", d, ex);
                this.evaluator.grading(0, comment, () -> false);
            }
        }
    }

    public <R> void each(Function<T, R> expected, Function<T, R> result, BiPredicate<R, R> matches) {
        this.each(
            d -> 1,
            expected, result, matches,
            d -> "Call " + d + " should return " + expected.apply(d),
            d -> "but returned " + result.apply(d)
        );
    }

    public <R> void each(int points, Function<T, R> expected, Function<T, R> result, BiPredicate<R, R> matches) {
        this.each(
            d -> points, 
            expected, result, matches, 
            d -> "Call " + d + " should return " + expected.apply(d),
            d -> "but returned " + result.apply(d)
        );
    }

    public <R> void each(
        Function<T, R> expected, 
        Function<T, R> result, 
        BiPredicate<R, R> matches,
        Function<T, String> call
    ) {
        this.each(
            d -> 1, 
            expected, result, matches, call,
            d -> "but returned " + result.apply(d)
        );
    }

    public <R> void each(
        Function<T, R> expected, 
        Function<T, R> result, 
        BiPredicate<R, R> matches,
        Function<T, String> call,
        Function<T, String> wrong
    ) {
        this.each(d -> 1, expected, result, matches, call, wrong);
    }
}