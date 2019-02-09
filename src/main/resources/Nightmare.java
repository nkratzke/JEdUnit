import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * This is just a test class.
 */
public class Nightmare {

    public static void main(String[] args) {
        System.out.println("Hello World");
        for (String arg : args);
        Stream.of(args).forEach(
            arg -> System.out.println(arg)
        );
    }

    public HashMap<Integer, String> method1(List<Integer> is) {
        return null;
    }

    public Map<Integer, String> method2(LinkedList<Integer> is) {
        Map<Integer, String> m = new TreeMap<>();
        return m;
    }

    public Map<Integer, String> method3(List<Integer> is) {
        TreeMap<Integer, String> m = new TreeMap<>();
        return m;
    }

    class SoUgly {
        class SoMuchMoreUgly {}
    }

    class NoBetter {}

    private void method4() {
        System.out.println("Only in the main");
    }

    public final static int CONST = 42;

    private int global = 13;

    private void method5() {
        System.exit(1);
    }
}