package uia.cor;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Yield control object.
 * 
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 * @param <R> The data type the generator sends back.
 */
public class Yield2Way<T, R> {
		
    private static final Logger logger = LogManager.getLogger(Yield2Way.class);

    private String id;
	
	private T value;
	
	private boolean closed;
	
	private Exception error;

	private final Consumer<Yield2Way<T, R>> iterable;
	
	private R callResult;

	/**
	 * Creates a Yield-Generator pair.
	 * 
	 * @param iterable The iterable program. 
	 * @return The generator.
	 */
	private Yield2Way(String id, Consumer<Yield2Way<T, R>> iterable) {
		this.id = id;
		this.iterable = iterable;
		this.closed = false;
	}
	
	/**
	 * Creates a Yield-Generator pair.
	 * 
	 * @param <T> The data type exchanges to the generator.
	 * @param <R> The data type the generator sends back.
	 * @param iterable The iterable program. 
	 * @return The generator.
	 */
	public static <T, R> Generator2Way<T, R> accept(Consumer<Yield2Way<T, R>> iterable) {
		return accept(null, iterable);
	}

	/**
	 * Creates a Yield-Generator pair.
	 * 
	 * @param <T> The data type exchanges to the generator.
	 * @param <R> The data type the generator sends back.
	 * @param yieldId The yield id.
	 * @param iterable The iterable program. 
	 * @return The generator.
	 */
	public static <T, R> Generator2Way<T, R> accept(String yieldId, Consumer<Yield2Way<T, R>> iterable) {
		final Yield2Way<T, R> yield = new Yield2Way<>(yieldId, iterable);
		synchronized(yield) {
			Thread th = new Thread(yield::running);
			th.setPriority(Thread.MAX_PRIORITY);
			th.start();
			try {
				yield.wait(1000);
			} catch (Exception e) {
				
			}
		}
		return new Generator2Way<>(yield);
	}
	
	/**
	 * Creates a Yield-Generator pair.
	 * 
	 * @param <T> The data type exchanges to the generator.
	 * @param <R> The data type the generator sends back.
	 * @param yieldable The iterable program. 
	 * @return The generator.
	 */
	public static <T, R> Generator2Way<T, R> accept(Yieldable2Way<T, R> yieldable) {
		return accept(null, yieldable::bind);
	}
	
	/**
	 * Creates a Yield-Generator pair.
	 * 
	 * @param <T> The data type exchanges to the generator.
	 * @param <R> The data type the generator sends back.
	 * @param yieldId The yield id.
	 * @param yieldable The iterable program. 
	 * @return The generator.
	 */
	public static <T, R> Generator2Way<T, R> accept(String yieldId, Yieldable2Way<T, R> yieldable) {
		return accept(yieldId, yieldable::bind);
	}
	
	/**
	 * Returns the id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets the result for call().
	 * 
	 * @param callResult The result for call().
	 */
	public synchronized void send(R callResult) {
		this.callResult = callResult;
	}

	/**
	 * Reports a error.
	 * 
	 * @param cause The cause of the error.
	 */
	public void error(Exception cause) {
		this.error = cause;
	}

	/**
	 * Checks if there is a next iteration.
	 * 
	 * @return True if there is a next iteration.
	 */
	public boolean next() {
		if(this.closed) {
			return false;
		}
		synchronized(this) {
			this.notifyAll();
			logger.debug(String.format("%s> next() is blocking, waiting call() to notify", this.id));	
			try {
				this.wait();
			} catch (Exception e) {

			}
		}
		return !this.closed;
	}

	/**
	 * Submits a new value to the paired generator.
	 * 
	 * @param value The new value.
	 * @return The result sent back from the generator.
	 * @throws YieldException Something wrong.
	 */
	public R call(T value) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = value;
			this.notifyAll();
			try {
				logger.debug(String.format("%s> call(v) is blocking, waiting send() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		testError();
		return this.callResult;
	}

	/**
	 * Submits a new value to the paired generator.
	 * 
	 * @param supplier The function to get the new value..
	 * @return The result sent back from the generator.
	 * @throws YieldException Something wrong.
	 */
	public R call(Supplier<T> supplier) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = supplier.get();
			this.notifyAll();
			try {
				logger.debug(String.format("%s> call(s) is blocking, waiting send() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		testError();
		return this.callResult;
	}
	
	/**
	 * Returns the final result of the iteration.
	 * 
	 * @return The result.
	 */
	public R getResult() {
		return this.callResult;
	}

	/**
	 * Returns the current value.
	 * 
	 * @return The current value.
	 */
	public synchronized T getValue() {
		return this.value;
	}

	/**
	 * Closes the iteration .
	 * 
	 */
	public synchronized void close() {
		this.closed = true;
		logger.debug(String.format("%s> close()", this.id));	
		this.notifyAll();
	}

	/**
	 * Closes the iteration .
	 * 
	 * @param result The final result.
	 */
	public synchronized void close(R result) {
		this.closed = true;
		this.callResult = result;
		logger.debug(String.format("%s> close(%s)", this.id, result));	
		this.notifyAll();
	}

	/**
	 * Closes the iteration .
	 * 
	 * @param cause The cause to close the iteration.
	 */
	public synchronized void close(InterruptedException cause) {
		this.closed = true;
		this.error = cause;
		logger.debug(String.format("%s> close()", this.id));	
		this.notifyAll();
	}

	/**
	 * Closes the iteration .
	 * 
	 * @param result The final result.
	 * @param cause The cause to close the iteration.
	 */
	public synchronized void close(R result, InterruptedException cause) {
		this.closed = true;
		this.callResult = result;
		this.error = cause;
		logger.debug(String.format("%s> close()", this.id));	
		this.notifyAll();
	}
	
  	/**
  	 * Tests if the iteration is alive or not.
  	 * 
  	 * @return True if the iteration is alive.
  	 */
	public synchronized boolean isAlive() {
		return !this.closed;
	}
	
  	/**
  	 * Tests if the iteration is closed or not.
  	 * 
  	 * @return True if the iteration is closed.
  	 */
	public synchronized boolean isClosed() {
		return this.closed;
	}
	
	private void running() {
		try {
			if(this.id == null) {
				this.id = "yield-" + Thread.currentThread().getId();
			}

			synchronized(this) {
				try {
					logger.debug(String.format("%s> running()", this.id));	
					this.notifyAll();
					this.wait();
				} catch (Exception ex) {

				}
			}
			this.iterable.accept(this);	// block until finishing iteration.
		}
		catch(Exception ex) {
			logger.error(String.format("%s> ruuning() failed", this.id), ex);	
		}
		finally {
			logger.debug(String.format("%s> ruuning() done", this.id));	
			close();
		}
	}
	
	private void testError() throws YieldException {
		if(this.error != null) {
			Exception ex = this.error;
			this.error = null;
			throw new YieldException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public String toString() {
		return this.id;
	}
	
	
}
