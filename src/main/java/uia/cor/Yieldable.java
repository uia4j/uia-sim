package uia.cor;

import java.util.function.Supplier;

/**
 * The abstract yield runner.
 * 
 * @author Kan
 *
 * @param <T> The data  type exchanges to the generator.
 */
public abstract class Yieldable<T> {
	
	private Yield<T> yield;
	
	/**
	 * binds a yield object with this runner of the iteration.
	 * 
	 * @param yield The yield object.
	 */
	protected final void bind(Yield<T> yield) {
		this.yield = yield;
		run();
	}

	/**
	 * Yields a value to the generator.
	 * 
	 * @param value The value.
	 */
	protected void yield(T value) {
		this.yield.call(value);
	}

	/**
	 * Yields a value to the generator.
	 * 
	 * @param s The value provider.
	 */
	protected void yield(Supplier<T> s) {
		this.yield.call(s);
	}

	/**
	 * Gets the yield object.
	 * 
	 * @return The yield object.
	 */
	protected Yield<T> yield() {
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
	 * Runs the iteration.
	 * 
	 */
	protected abstract void run();
}
