package uia.cor;

/**
 * The generator pairs with Yeild2Way.
 * 
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 * @param <R> The data type the generator sends back.
 */
public class Generator2Way<T, R> {

	private final Yield2Way<T, R> yield;

	/**
	 * The constructor.
	 * 
	 * @param yield The yield object.
	 */
	public Generator2Way(Yield2Way<T, R> yield) {
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
	 * Sends a result to the iteration.
	 * 
	 * @param callResult The result.
	 */
	public synchronized void send(R callResult) {
		this.yield.send(callResult);
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
	 * Returns the final result of the iteration.
	 * 
	 * @return The result.
	 */
	public R getResult() {
		return this.yield.getResult();
	}

	/**
	 * Closes the iteration.
	 * 
	 */
	public synchronized void close() {
		this.yield.close();
	}

	/**
	 * Closes the iteration.
	 * 
	 * @param cause The cause.
	 */
	public synchronized void close(InterruptedException cause) {
		this.yield.close(cause);
	}

	/**
	 * Closes the iteration.
	 * 
	 * @param value The final value.
	 * @param cause The cause.
	 */
	public synchronized void close(R value, InterruptedException cause) {
		this.yield.close(value, cause);
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
	 * Returns the last result.
	 * 
	 * @return True if there is a new value.
	 */
	public synchronized NextResult<T> nextResult() {
		boolean hasNext = this.yield.next();
		return new NextResult<T>(hasNext, this.yield.getValue());
	}

	
	/**
	 * Returns the last result and tests if there is a next iteration or not.
	 * 
	 * @param callResult The result sent back to the iteration.
	 * @return The next information.
	 */
	public synchronized NextResult<T> nextResult(R callResult) {
		this.yield.send(callResult);
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
