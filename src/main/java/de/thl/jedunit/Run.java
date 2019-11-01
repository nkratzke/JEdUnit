package de.thl.jedunit;

import java.util.function.Supplier;

/**
 * Helper class that compromises several methods
 * to handle runtime constraints for submission logic
 * like
 * 
 * - timeouts
 * - stopwatch (for performance comparisons etc.) // TO BE DONE
 * 
 * @author Nane Kratzke
 */
public class Run {

    public static <T> T withTimeout(Supplier<T> logic, int n) throws Exception {
        // To be done
        return logic.get();
    }

}