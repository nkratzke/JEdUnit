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
        for (int i : Arrays.asList(1, 10, 20, 27, 48)) {
            String tc = String.format("Testcase test(%d)", i);
            grading(20, tc, () -> Main.test(i) == Solution.test(i));
        }
    }

    private String normalize(CallableDeclaration callable) {
        return callable.getDeclarationAsString(true, true, false);
    }

    private String normalize(FieldDeclaration field) {
        String modifiers = field.getModifiers().stream().map(m -> (m + "").toLowerCase()).collect(Collectors.joining(" "));
        String type = field.getCommonType().asString();
        return field.getVariables()
                    .stream()
                    .map(v -> String.format("%s %s %s", modifiers, type, v.getName()))
                    .collect(Collectors.joining("\n"));
    }

    private String normalize(ClassOrInterfaceDeclaration clazz) {
        String modifiers = clazz.getModifiers().stream().map(m -> (m + "").toLowerCase()).collect(Collectors.joining(" "));
        String name = clazz.getNameAsString();
        String extend = clazz.getExtendedTypes().stream().map(t -> t.asString()).collect(Collectors.joining(", "));
        String implement = clazz.getImplementedTypes().stream().map(t -> t.asString()).collect(Collectors.joining(", "));
        return String.format("%s class %s extends %s implements %s", modifiers, name, extend, implement).trim();
    }

    @Check
    void futherTestcases() {
        comment("Further testcases (unknown test cases)");
        try {
            Parser ast = parse("Main.java");
            ast.select(ClassOrInterfaceDeclaration.class).stream().forEach(clazz -> System.out.println(normalize(clazz)));
            ast.select(CallableDeclaration.class).stream().forEach(m -> System.out.println(normalize(m)));   
            ast.select(FieldDeclaration.class).stream().forEach(field -> System.out.println(normalize(field)));
        } catch(Exception ex) {
            comment(ex + "");
        }
        // To be done ...
    }
}
