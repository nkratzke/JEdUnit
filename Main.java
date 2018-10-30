import java.util.stream.Stream;
import java.awt.*;

/**
 * Main class for VPL assignments.
 * Please provide this file to your students.
 * They should start to build their solution with this file.
 * It is recommended to replace this comment with a
 * descriptive and understandable assignment text.
 * @author Nane Kratzke
 */
class Main {

    class InnerClass {

        class InnerInnerClass {

        }

    }

    public static int test(int i) {
        for (int j = 1; j <= i; j++) {
            System.out.print(i);
        }
        Stream.of().forEach(j -> System.out.println(j));
        return 42;
    }

    /**
     * Here is where everything starts.
     * @param args Command line parameters (not evaluated)
     */
    public static void main(String[] args) {
        System.out.println("Your solution is missing here.");
        while (args.length > 10);
    }
}