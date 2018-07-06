import java.util.*;
import java.util.stream.*;
import java.lang.reflect.*;

/**
 * Please add your test cases for evaluation here.
 * - Please provide meaningful remarks for your students in grading() calls.
 * - All grading() calls should sum up to 100.
 * - All methods that start with "test" will be executed automatically.
 * - If this sounds similar to unit testing - this is intended ;-)
 */
class Checks extends Evaluator {

    public void testStudentsSubmission() {
        grading(10, "Replace this with a meaningfull error message.", () -> "apple".equals("banana"));             
    }
}