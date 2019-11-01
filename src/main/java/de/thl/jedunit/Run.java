package de.thl.jedunit;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Helper class that compromises several methods
 * to handle runtime constraints for submission logic
 * like
 * 
 * - timeouts
 * - stopwatch (for performance comparisons etc.) // TO BE DONE
 * 
 * @author Nane Kratzke
 */
public class Run {

    public static <T> T withTimeout(Supplier<T> logic, int timeout) throws Throwable {
		Callable<T> submission = new Logic<T>(logic);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<T> future = executor.submit(submission);
		try {
			return future.get(timeout, TimeUnit.SECONDS);			
		} catch (TimeoutException ex) {
			throw new TimeoutException("runtime > " + timeout + " seconds. Endless loop?");
		} catch (ExecutionException ex) {
            throw ex.getCause();
        } finally {
            future.cancel(true);
			executor.shutdownNow();
		}
    }
}

class Logic<T> implements Callable<T> {
	private Supplier<T> logic;
	public Logic(Supplier<T> l) {
		this.logic = l;
	}
	public T call() { return this.logic.get(); }		
}