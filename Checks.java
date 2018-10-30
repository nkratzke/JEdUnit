import java.io.FileNotFoundException;
import java.util.Arrays;

import com.github.javaparser.ast.body.MethodDeclaration;
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

    @Restriction
    void noPrintlns() throws FileNotFoundException {
        /*
        this.parse("Main.java")
            .select(MethodDeclaration.class, (MethodDeclaration m) -> !m.getDeclarationAsString(false, false, false).equals("void main(String[])"))
            .select(MethodCallExpr.class, (MethodCallExpr expr) -> expr.toString().contains("System.out.println"))
            .annotate("Do not use System.out.println in methods, except main().")
            .stream()
            .forEach(System.out::println);
            /*
            .filter(m -> !((MethodDeclaration)m).getName().equals("main"))
            .select(MethodCallExpr.class)
            .map(call -> ((MethodCallExpr)call).getArguments())
            .forEach(args -> System.out.println(args)
        
        );*/
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
