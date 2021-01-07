package uia.cor;

public final class Generator<T> {

	private final Yield<T> yield;

	public Generator(Yield<T> yield) {
		this.yield = yield;
	}
	
	public synchronized boolean interrupt(String message) {
		return interrupt(new InterruptedException(message));
	}
	
	public synchronized boolean interrupt(InterruptedException cause) {
		return this.yield.interrupt(cause);
	}
	
	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized boolean next() {
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
	public synchronized void close() {
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
