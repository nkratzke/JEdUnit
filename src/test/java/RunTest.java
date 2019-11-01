import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import de.thl.jedunit.Config;
import de.thl.jedunit.Run;


public class RunTest {

    @Test public void withoutTimeout() throws Throwable {
        assertTrue(Run.withTimeout(() -> 42, Config.TIMEOUT).equals(42));
        assertFalse(Run.withTimeout(() -> 42, Config.TIMEOUT).equals(0));
        assertTrue(Run.withTimeout(() -> "Hello", Config.TIMEOUT).equals("Hello"));
        assertFalse(Run.withTimeout(() -> "Hello", Config.TIMEOUT).equals(""));
    }

    @Test(expected=TimeoutException.class)
    public void withTimeout() throws Throwable {
        Supplier<Integer> endless = () -> {
            int n = 0;
            while (n <= n);
            return 42;
        };
        Run.withTimeout(endless, Config.TIMEOUT);
    }

    @Test(expected=ArithmeticException.class)
    public void withException() throws Throwable {
        Supplier<Integer> error = () -> {
            int n = 0;
            return 42 / n;
        };
        Run.withTimeout(error, Config.TIMEOUT);
    }
}