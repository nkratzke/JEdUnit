import java.lang.reflect.*;
import java.util.*;

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

    private static final String lowerCamelCase = "[a-z]+((\\d)|([A-Z0-9][a-z0-9]+))*([A-Z])?";
    private static final String upperCamelCase = "((\\d)|([A-Z0-9][a-z0-9]+))*([A-Z])?";
    private static final String capitalCase = "[A-Z0-9_]+";
    private static List<String> notAllowed = Arrays.asList(
        "java.util.LinkedList", "java.util.ArrayList", "java.util.Stack", "java.util.Vector",
        "java.util.TreeMap", "java.util.HashMap", "java.util.LinkedHashMap", "java.util.WeakHashMap"
    );

    public void testCodingRestrictions() {
        // Checks that submissions does not access the reference solution or try to run code injection attacks.
        // Adapt it accordingly, in cases you do not provide your reference solution in a Solution.java file.
        degrading(100, "Cheat check", () -> assure("Main", c -> c.hasNo("Solution", "System.exit", "Grade :=>>")));
        
        degrading(20, 
            "Avoid global variables (datafields). They are not necessary to solve this excercise.", 
            () -> assure("Main", c -> c.hasNoFields())
        );

        degrading(20, 
            "Avoid inner 'ugly' classes. They are not necessary to solve this excercise.", 
            () -> assure("Main", c -> c.hasNoInnerClasses())
        );

        degrading(20,
            "Do not use concrete collection classes like " + notAllowed + " as return types. Use the interfaces Map and List instead.",
            () -> assure("Main", c -> c
                .methods()
                .noneMatch(m -> notAllowed.contains(((Method)m).getReturnType().getName()))
            )
        );
    }
    
    public void testNamingConventions() {
        degrading(10, 
            "Please, use lowerCamelCase notation for all datafields.", 
            () -> assure("Main", c -> c.fields().allMatch(f -> ((Field)f).getName().matches(lowerCamelCase)))
        );
        degrading(10, 
            "Please, use lowerCamelCase notation for all methods.", 
            () -> assure("Main", c -> c.methods().allMatch(m -> ((Method)m).getName().matches(lowerCamelCase)))
        );
        degrading(10,
            "Please, use CAPITAL notation for all constants.",
            () -> assure("Main", c -> c.constants().allMatch(con -> ((Field)con).getName().matches(capitalCase)))
        );
    }

}
