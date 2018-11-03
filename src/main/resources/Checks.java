import java.util.Arrays;

import de.thl.jedunit.Check;
import de.thl.jedunit.Constraints;

/**
 * Please add your test cases for evaluation here.
 * - Please provide meaningful remarks for your students in grading() calls.
 * - All grading() calls should sum up to 100.
 * - If you give more than 100 points it will be truncated. 
 * - This enable bonus rules (e.g. giving 120 points instead of 100) to tolerate some errors worth 20 points. 
 * - All methods that start with "test" will be executed automatically.
 * - If this sounds similar to unit testing - this is intended ;-)
 */
public class Checks extends Constraints {

    @Override
    protected void configure() {
        super.configure();

        // Constraints.ALLOWED_IMPORTS = Arrays.asList("java.util");

        // Constraints.ALLOW_LOOPS = false;                 // default: true
        // Constraints.LOOP_PENALTY = 100;
    
        // Constraints.ALLOW_LAMBDAS = false;               // default: true
        // Constraints.LAMBDA_PENALITY = 25;
    
        // Constraints.ALLOW_INNER_CLASSES = true;          // default: false
        // Constraints.INNER_CLASS_PENALTY = 100;
    
        // Constrainst.ALLOW_GLOBAL_VARIABLES = true;       // default: false
        // Constraints.GLOBAL_VARIABLE_PENALTY = 25;
    
        // Constraints.CHECK_COLLECTION_INTERFACES = false; // default: true
        // COLLECTION_INTERFACE_PENALTY = 25;
    
        // Constraints.ALLOW_CONSOLE_OUTPUT = true;         // default: false
        // Constraints.CONSOLE_OUTPUT_PENALTY = 25;
    }

    @Check
    public void examples() {
        comment("Provided example calls");
        grading(5, "Counting 'o' in \"Hello World\" must return 2.", 
            () -> Main.countChars('o', "Hello World") == Solution.countChars('o', "Hello World")
        );
        grading(5, "Counting 'w' in 'Hello World' must return 2.", 
            () -> Main.countChars('w', "Hello World") == Solution.countChars('w', "Hello World")
        );
    }

    @Check
    public void furtherTestcases() {
        comment("Boundary testcases (unknown test cases)");
    }

    @Check
    public void randomizedTestcases() {
        comment("Randomized testcases");
    }
}
