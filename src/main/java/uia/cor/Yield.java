package uia.cor;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Yield<T> {
		
    private static final Logger logger = LogManager.getLogger(Yield.class);

    private String id;
	
	private T value;
	
	private boolean closed;
	
	private InterruptedException interrupted;

	private final Consumer<Yield<T>> iterable;

	private Yield(String id, Consumer<Yield<T>> iterable) {
		this.id = id;
		this.iterable = iterable;
		this.closed = false;
	}
	
	public static <T> Generator<T> accept(Consumer<Yield<T>> iterable) {
		return accept(null, iterable);
	}

	public static <T> Generator<T> accept(String yieldId, Consumer<Yield<T>> iterable) {
		final Yield<T> yield = new Yield<>(yieldId, iterable);
		synchronized(yield) {
			Thread th = new Thread(yield::running);
			th.setPriority(Thread.MAX_PRIORITY);
			th.start();
			try {
				yield.wait(1000);
			} catch (Exception e) {
				
			}
		}
		return new Generator<>(yield);
	}
	
	public String getId() {
		return this.id;
	}

	/**
	 * Checks if there is a new value or not.
	 * 
	 * @return True if there is a new value.
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

	public boolean interrupt(InterruptedException cause) {
		if(this.closed) {
			return false;
		}
		synchronized(this) {
			this.interrupted = cause;
			this.notifyAll();
			logger.debug(String.format("%s> interrupt() release call()", this.id));	
			try {
				this.wait();
			} catch (Exception e) {

			}
		}
		return !this.closed;
	}

	/**
	 * Submit a new value to the generator of this instance.
	 * 
	 * @param value The new value.
	 * @throws YieldException
	 */
	public void call(T value) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = value;
			this.notifyAll();
			try {
				logger.debug(String.format("%s> call(v) is blocking, waiting next() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		testInterrupt();
	}

	/**
	 * Submit a new value.
	 * 
	 * @param supplier The function to get the new value..
	 */
	public void call(Supplier<T> supplier) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = supplier.get();
			this.notifyAll();
			try {
				logger.debug(String.format("%s> call(s) is blocking, waiting next() to release", this.id));	
				this.wait();
			} catch (Exception ex) {

			}
		}

		testInterrupt();
	}

	/**
	 * Submit the last value.
	 * 
	 * @param value The last value.
	 */
	public void callLast(T value) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = value;
			this.notifyAll();
			logger.debug(String.format("%s> callLast(v) notify next() to get new value", this.id));	
		}
		this.closed = true;
	}

	/**
	 * Submit the last value.
	 * 
	 * @param supplier The function to get the last value..
	 */
	public void callLast(Supplier<T> supplier) throws YieldException {
		if(this.closed) {
			throw new YieldException("The yield is closed");
		}

		synchronized(this) {
			this.value = supplier.get();
			this.notifyAll();
			logger.debug(String.format("%s> callLast(s) notify next() to get new value", this.id));	
		}
		this.closed = true;
	}
	
	/**
	 * Returns the current value.
	 * @return
	 */
	public synchronized T getValue() {
		return this.value;
	}

	public synchronized void close() {
		this.closed = true;
		logger.debug(String.format("%s> close()", this.id));	
		this.notifyAll();
	}
	
	public synchronized boolean isAlive() {
		return !this.closed;
	}
	
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
			this.iterable.accept(this);	// blocking
		}
		catch(Exception ex) {
			logger.error(String.format("%s> ruuning() failed", this.id), ex);	
		}
		finally {
			logger.debug(String.format("%s> ruuning() done", this.id));	
			close();
		}
	}
	
	private void testInterrupt() throws YieldException {
		if(this.interrupted != null) {
			InterruptedException ex = this.interrupted;
			this.interrupted = null;
			throw new YieldException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public String toString() {
		return this.id;
	}
	
	
}
