import java.util.List;

public class A<T, R> extends C implements DD, EE, FF {
    
    public int datafield = 43, other=17;
    public static String CONST = "";
    protected List<A> next;

    public A(int a, String s) {
    }

    A(String s) {
    }

    public void method(C c, A a) {
    }

    public A method(A a) {
        return null;
    }

    private int test(A a) {
        return 0;
    }

}