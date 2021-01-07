package uia.cor;

public class Generator2Way<T, R> {

	private final Yield2Way<T, R> yield;

	public Generator2Way(Yield2Way<T, R> yield) {
		this.yield = yield;
	}
	
	public synchronized void error(String message) {
		error(new YieldException(message));
	}
	
	public synchronized void error(Exception cause) {
		this.yield.error(cause);
	}
	
	public synchronized void send(R callResult) {
		this.yield.send(callResult);
	}
	
	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a next iteration.
	 */
	public synchronized boolean next() {
		return this.yield.next();
	}
	
	/**
	 * Checks if there is a new value or not.<br>
	 * The method calls 2 methods: send(value), next().
	 * 
	 * @param callResult The result for previous call().
	 * @return True if there is a next iteration.
	 */
	public synchronized boolean next(R callResult) {
		this.yield.send(callResult);
		return this.yield.next();
	}
		
	/**
	 * Checks if there is a new value or not.<br>
	 * The method calls 2 methods: error(value), next().
	 * 
	 * @param cause The cause.
	 * @return True if there is a next iteration.
	 */
	public synchronized boolean errorNext(Exception cause) {
		this.yield.error(cause);
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
	 * Stops the yield control.
	 * 
	 */
	public synchronized void close() {
		this.yield.close();
	}

	/**
	 * Stops the iteration.
	 * 
	 * @param cause The cause.
	 */
	public synchronized void close(InterruptedException cause) {
		this.yield.close(cause);
	}

	/**
	 * Stops the iteration.
	 * 
	 * @param value The final value.
	 * @param cause The cause.
	 */
	public synchronized void close(R value, InterruptedException cause) {
		this.yield.close(value, cause);
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
