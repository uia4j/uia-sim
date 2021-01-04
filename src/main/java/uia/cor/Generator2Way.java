package uia.cor;

public class Generator2Way<T, R> {

	private final Yield2Way<T, R> yield;

	public Generator2Way(Yield2Way<T, R> yield) {
		this.yield = yield;
	}
	
	public synchronized void interrupt() {
		this.yield.interrupt();
		
	}
	
	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized boolean next() {
		return this.yield.send(null);
	}
	
	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized boolean next(R callResult) {
		return this.yield.send(callResult);
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
		boolean hasNext = this.yield.send(null);
		return new NextResult<T>(hasNext, this.yield.getValue());
	}

	
	/**
	 * Returns the last result.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized NextResult<T> nextResult(R callResult) {
		boolean hasNext = this.yield.send(callResult);
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
