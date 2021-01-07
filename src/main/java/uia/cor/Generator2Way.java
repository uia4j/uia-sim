package uia.cor;

public class Generator2Way<T, R> {

	private final Yield2Way<T, R> yield;

	public Generator2Way(Yield2Way<T, R> yield) {
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
	public synchronized void send(R callResult) {
		this.yield.send(callResult);
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
	 * Checks if there is a new value or not.
	 * 
	 * @param callResult The result for previous call().
	 * @return True if there is a new value.
	 */
	public synchronized boolean next(R callResult) {
		this.yield.send(callResult);
		return this.yield.next();
	}
	
	public R getResult() {
		return this.yield.getResult();
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
	 * Returns the last result.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized NextResult<T> nextResult(R callResult) {
		this.yield.send(callResult);
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
