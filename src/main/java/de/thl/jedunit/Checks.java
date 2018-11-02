package de.thl.jedunit;

import java.util.Arrays;

/**
 * Please add your test cases for evaluation here.
 * - Please provide meaningful remarks for your students in grading() calls.
 * - All grading() calls should sum up to 100.
 * - If you give more than 100 points it will be truncated. 
 * - This enable bonus rules (e.g. giving 120 points instead of 100) to tolerate some errors worth 20 points. 
 * - All methods that start with "test" will be executed automatically.
 * - If this sounds similar to unit testing - this is intended ;-)
 */
class Checks extends Constraints {

    @Override
    protected void configure() {
        super.configure();
        Constraints.ALLOWED_IMPORTS = Arrays.asList("java.util");
        Constraints.ALLOW_LOOPS = false;
        Constraints.ALLOW_LAMBDAS = false;
    }

    @Check
    void examples() {
        comment("Provided example calls");
    }

    @Check
    void futherTestcases() {
        comment("Boundary testcases (unknown test cases)");
    }

    @Check
    void randomizedTestcases() {
        comment("Randomized testcases");
    }
}
