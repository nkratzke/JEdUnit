import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;

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
        Evaluator.ALLOWED_IMPORTS = Arrays.asList("java.util");
        Evaluator.ALLOW_LOOPS = false;
        Evaluator.ALLOW_LAMBDAS = false;
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
