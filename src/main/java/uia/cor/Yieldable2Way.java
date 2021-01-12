package uia.cor;

import java.util.function.Supplier;

/**
 * The abstract yield runner.
 * 
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 * @param <R> The data type the generator sends back.
 */
public abstract class Yieldable2Way<T, R> {
	
	private Yield2Way<T, R> yield;
	
	/**
	 * binds a yield object with this runner of the iteration.
	 * 
	 * @param yield The yield object.
	 */
	protected final void bind(Yield2Way<T, R> yield) {
		this.yield = yield;
		run();
	}

	/**
	 * Yields a value to the generator.
	 * 
	 * @param value The value.
	 * @return The result sent back from the generator.
	 */
	protected R yield(T value) {
		return this.yield.call(value);
	}

	/**
	 * Yields a value to the generator.
	 * 
	 * @param s The value provider.
	 * @return The result sent back from the generator.
	 */
	protected R yield(Supplier<T> s) {
		return this.yield.call(s);
	}

	/**
	/**
	 * Gets the yield object.
	 * 
	 * @return The yield object.
	 */
	protected Yield2Way<T, R> yield() {
		return this.yield;
	}
	
	/**
	 * Closes the iteration.
	 * 
	 */
	protected void close() {
		this.yield.close();
	}

	/**
	 * Closes the iteration.
	 * 
	 * @param value The final result.
	 */
	protected void close(R value) {
		this.yield.close(value);
	}
	
	/**
	 * Runs the iteration.
	 * 
	 */
	protected abstract void run();
}
