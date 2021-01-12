package uia.cor;

/**
 * The generator pairs with Yield.
 * 
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 */
public final class Generator<T> {

	private final Yield<T> yield;

	/**
	 * The constructor.
	 * 
	 * @param yield The yield object.
	 */
	public Generator(Yield<T> yield) {
		this.yield = yield;
	}
	
	/**
	 * Reports error to the iteration.
	 * 
	 * @param message The error message.
	 */
	public synchronized void error(String message) {
		error(new YieldException(message));
	}
	
	/**
	 * Reports error to the iteration.
	 * 
	 * @param cause The cause.
	 */
	public synchronized void error(Exception cause) {
		this.yield.error(cause);
	}

	/**
	 * Checks if there is a next iteration or not.<br>
	 * 
	 * @return True if there is a next iteration.
	 */
	public synchronized boolean next() {
		return this.yield.next();
	}

	/**
	 * Checks if there is a next iteration or not.<br>
	 * The method calls 2 methods: error(value), next().
	 * 
	 * @param cause The cause.
	 * @return True if there is a next iteration.
	 */
	public synchronized boolean errorNext(Exception cause) {
		this.yield.error(cause);
		return this.yield.next();
	}

	/**
	 * Closes the iteration.
	 * 
	 */
	public synchronized void stop() {
		this.yield.close();
	}

	/**
	 * Closes the iteration.
	 * 
	 * @param cause The cause.
	 */
	public synchronized void stop(InterruptedException cause) {
		this.yield.close(cause);
	}

	/**
	 * Tests if the iteration is closed.
	 * 
	 * @return True if the iteration is closed.
	 */
	public boolean isClosed() {
		return this.yield.isClosed();
	}
	
	/**
	 * Returns the last result and tests if there is a next iteration or not.
	 * 
	 * @return The next information.
	 */
	public synchronized NextResult<T> nextResult() {
		boolean hasNext = this.yield.next();
		return new NextResult<T>(hasNext, this.yield.getValue());
	}
	
	/**
	 * Returns the current value of the iteration.
	 * 
	 * @return The current value.
	 */
	public synchronized T getValue() {
		return this.yield.getValue();
	}
}
