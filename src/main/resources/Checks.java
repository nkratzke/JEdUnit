import java.util.stream.Stream;

import static de.thl.jedunit.Randomized.*;

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
    public void configure() {
        super.configure();

        // Constraints.ALLOWED_IMPORTS = Arrays.asList("java.util");

        // Constraints.ALLOW_LOOPS = false;                 // default: true
        // Constraints.LOOP_PENALTY = 100;

        // Constraints.ALLOW_METHODS = false;               // default: true
        // Constraints.METHOD_PENALTY = 100;
    
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
        grading(5, "Counting 'w' in \"Hello World\" must return 2.", 
            () -> Main.countChars('w', "Hello World") == Solution.countChars('w', "Hello World")
        );
    }

    @Check
    public void furtherTestcases() {
        comment("Boundary testcases (unknown test cases)");
        Stream.of("", "x", "X", "ax", "Xa").forEach(s -> {
            Stream.of('x', 'X').forEach(c -> {
                grading(5, String.format("Do you considered cases like countChars('%s', \"%s\")?", c, s), 
                    () -> Main.countChars(c, s) == Solution.countChars(c, s)
                );
            });
        });
    }

    @Check
    public void randomizedTestcases() {
        comment("Randomized testcases");
        String r = "[a-zA-Z!?&%$§]{5,17}";
        char c = c("[a-z]");
        char C = Character.toUpperCase(c);
        Stream.of(
            s(c + "{1,7}", r, r),
            s(r.toUpperCase(), r, C + "{1,7}"),
            s(r, C + "{1,7}", r.toLowerCase()),
            s(r, c + "{1,7}", r.toUpperCase())
        ).forEach(s -> {
            grading(5, String.format("Do you considered cases like countChars('%s', \"%s\")?", c, s), 
                () -> Main.countChars(c, s) == Solution.countChars(c, s)
            );
            grading(5, String.format("Do you considered cases like countChars('%s', \"%s\")?", C, s), 
                () -> Main.countChars(C, s) == Solution.countChars(C, s)
            );
        });
    }
}
