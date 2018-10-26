import java.lang.reflect.*;
import java.util.*;
import com.github.javaparser.ast.stmt.*;


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

    public void testCodingRestrictions() {
        // Checks that submissions does not access the reference solution or try to run code injection attacks.
        // Adapt it accordingly, in cases you do not provide your reference solution in a Solution.java file.
        
        abortOn("Cheat detected", () -> check("Main.java", c -> c.noContainOf("Solution", "System.exit", "Grade :=>>")));
        degrading(100, "Recursion check", () -> check("Main.java", c -> c.noStatementOf(WhileStmt.class, ForStmt.class, DoStmt.class)));
        degrading(10, "Avoid global variables.", () -> check("Main.java", c -> c.noDataFields()));
        degrading(10, "Avoid inner classes.", () -> check("Main.java", c -> c.noInnerClasses()));

        Class[] notAllowedCollectionImplementations = { HashMap.class, TreeMap.class, HashSet.class, LinkedList.class, ArrayList.class };
        degrading(10, "Use Map, List, and Set interfaces.", () -> 
            check("Main.java", c -> c.noParametersOf(notAllowedCollectionImplementations)) &
            check("Main.java", c -> c.noReturnTypesOf(notAllowedCollectionImplementations))
        );
    }
}
