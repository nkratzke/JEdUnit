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
class Checks extends Evaluator {

    @Override
    protected void configure() {
        super.configure();
        Evaluator.ALLOW_LOOPS = false;
    }

    @Check
    void examples() {
        comment("Provided example calls");
        for (int i : Arrays.asList(1, 10, 20, 27, 48)) {
            String tc = String.format("Testcase test(%d)", i);
            grading(20, tc, () -> Main.test(i) == Solution.test(i));
        }
    }

    @Check
    void futherTestcases() {
        comment("Further testcases (unknown test cases)");
        // To be done ...
    }
}
