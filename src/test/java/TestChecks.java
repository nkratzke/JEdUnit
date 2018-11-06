import de.thl.jedunit.*;
import java.util.stream.*;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import static de.thl.jedunit.Randomized.*;

public class TestChecks extends Constraints {
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

    @Check public void failingTest() {
        comment("This test should fail completely");
        double d[] = {};
        d[0] *= 2;
    }

    @Check public void failingCheck() {
        comment("Failing Checks test");
        grading(100, "This check will fail", () -> check("NotExisting.java", (ast) -> ast.select(ClassOrInterfaceDeclaration.class).exists()));
        grading(100, "This check will also fail", () -> { double[] d = {}; return d[0] == 0; });
    }

    @Check public void examplesForPenalizing() {
        comment("Penalizing checks");
        penalize(3, "This should reduce points", () -> true);
        penalize(3, "This message should not occur", () -> false);
        penalize(3, "This should not reduce points", () -> { double[] d = {}; return d[0] == 0; });
    }

    @Check public void examplesForAbortOn() {
        comment("Aborting checks");
        abortOn("This kind of abort should not reduce points", () -> { int[] i = {}; return i[0] == 0; });
        abortOn("This message should not occur", () -> false);
        abortOn("This should result in zero points", () -> true);
    }
}