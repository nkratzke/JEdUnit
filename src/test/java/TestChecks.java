import static de.thl.jedunit.DSL.c;
import static de.thl.jedunit.DSL.inspect;
import static de.thl.jedunit.DSL.s;

import java.util.stream.Stream;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import de.thl.jedunit.Constraints;
import de.thl.jedunit.Test;

public class TestChecks extends Constraints {
    
    @Test(weight=0.25, description="Provided example calls")
    public void examples() {
        grading(5, "Counting 'o' in \"Hello World\" must return 2.", 
            () -> Main.countChars('o', "Hello World") == Solution.countChars('o', "Hello World")
        );
        grading(5, "Counting 'w' in \"Hello World\" must return 2.", 
            () -> Main.countChars('w', "Hello World") == Solution.countChars('w', "Hello World")
        );
    }

    @Test(weight=0.25, description="Boundary testcases")
    public void furtherTestcases() {
        Stream.of("", "x", "X", "ax", "Xa").forEach(s -> {
            Stream.of('x', 'X').forEach(c -> {
                grading(5, String.format("Do you considered cases like countChars('%s', \"%s\")?", c, s), 
                    () -> Main.countChars(c, s) == Solution.countChars(c, s)
                );
            });
        });
    }

    @Test(weight=0.5, description="Randomized testcases")
    public void randomizedTestcases() {
        String r = "[a-zA-Z!?&%$§]{5,17}";
        char c = c("[a-z]");
        char C = Character.toUpperCase(c);
        Stream.of(
            s(c + "{1,7}", r, r),
            s(r.toUpperCase(), r, C + "{1,7}"),
            s(r, C + "{1,7}", r.toLowerCase()),
            s(r, c + "{1,7}", r.toUpperCase())
        ).forEach(s -> {
            grading(5, String.format("Do you considered cases like\n countChars('%s', \"%s\")?", c, s), 
                () -> Main.countChars(c, s) == Solution.countChars(c, s)
            );
            grading(5, String.format("Do you considered cases like\n countChars('%s', \"%s\")?", C, s), 
                () -> Main.countChars(C, s) == Solution.countChars(C, s)
            );
        });
    }

    @Test(weight=0.0, description="This test should fail completely")
    public void failingTest() {
        double d[] = {};
        d[0] *= 2;
    }

    @Test(weight=0.0, description="Failing Checks test")
    public void failingCheck() {
        grading(100, "This check will fail", () -> inspect("NotExisting.java", (ast) -> ast.select(ClassOrInterfaceDeclaration.class).exists()));
        grading(100, "This check will also fail", () -> { double[] d = {}; return d[0] == 0; });
    }

    @Test(weight=0.0, description="Penalizing checks")
    public void examplesForPenalizing() {
        penalize(3, "This should reduce points", () -> true);
        penalize(3, "This message should not occur", () -> false);
        penalize(3, "This should not reduce points", () -> { double[] d = {}; return d[0] == 0; });
    }

    @Test(weight=0.0, description="Aborting checks")
    public void examplesForAbortOn() {
        abortOn("This kind of abort should not reduce points", () -> { int[] i = {}; return i[0] == 0; });
        abortOn("This message should not occur", () -> false);
        abortOn("This should result in zero points", () -> true);
    }
}