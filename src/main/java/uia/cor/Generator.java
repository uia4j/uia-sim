package uia.cor;

public final class Generator<T> {

	private final Yield<T> yield;

	public Generator(Yield<T> yield) {
		this.yield = yield;
	}
	
	/**
	 * Interrupts current and go to next iteration.
	 * 
	 * @param cause The cause.
	 * @return True if there is a next iteration.
	 */
	public synchronized void error(String message) {
		error(new YieldException(message));
	}
	
	/**
	 * Interrupts current and go to next iteration.
	 * 
	 * @param cause The cause.
	 * @return True if there is a next iteration.
	 */
	public synchronized void error(Exception cause) {
		this.yield.error(cause);
	}
	
	/**
	 * Stops the iteration.
	 * 
	 * @param cause The cause.
	 */
	public synchronized void stop(InterruptedException cause) {
		this.yield.close(cause);
	}

	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized boolean next() {
		return this.yield.next();
	}

	/**
	 * Checks if there is a new value or not.<br>
	 * The method calls 2 methods: error(value), next().
	 * 
	 * @param cause The cause.
	 * @return True if there is a new value.
	 */
	public synchronized boolean errorNext(Exception cause) {
		this.yield.error(cause);
		return this.yield.next();
	}

	public boolean isClosed() {
		return this.yield.isClosed();
	}
	
	/**
	 * Returns the last result.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized NextResult<T> nextResult() {
		boolean hasNext = this.yield.next();
		return new NextResult<T>(hasNext, this.yield.getValue());
	}


	/**
	 * Cancels the yield control.
	 * 
	 */
	public synchronized void stop() {
		this.yield.close();
	}
	
	/**
	 * Returns the current value.
	 * 
	 * @return The current value.
	 */
	public synchronized T getValue() {
		return this.yield.getValue();
	}
}
