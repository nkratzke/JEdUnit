import java.lang.invoke.*;
import java.lang.reflect.*;

public class Evil {

    public static int solution(int test) {
        System.exit(1);
        return Solution.test(test);
    }

    public static void main(String[] args) {

    }
}