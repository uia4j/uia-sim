package uia.cor;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Yield2Way<T, R> {
		
    private static final Logger logger = LogManager.getLogger(Yield2Way.class);

    private String id;
	
	private T value;
	
	private boolean closed;
	
	private InterruptedException interrupted;

	private final Consumer<Yield2Way<T, R>> iterable;
	
	private R callResult;

	private Yield2Way(String id, Consumer<Yield2Way<T, R>> iterable) {
		this.id = id;
		this.iterable = iterable;
		this.closed = false;
	}
	
	public static <T, R> Generator2Way<T, R> accept(Consumer<Yield2Way<T, R>> iterable) {
		return accept(null, iterable);
	}

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
	 * Submit a new value.
	 * 
	 * @param value The new value.
	 */
	public R call(T value) throws Exception {
		if(this.closed) {
			return null;
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

		testInterrupt();
		return this.callResult;
	}
	
	
	public R getResult() {
		return this.callResult;
	}

	/**
	 * Submit a new value.
	 * 
	 * @param supplier The function to get the new value..
	 */
	public R call(Supplier<T> supplier) throws Exception {
		if(this.closed) {
			return null;
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

		testInterrupt();
		return this.callResult;
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

	public synchronized void close(R result) {
		this.closed = true;
		this.callResult = result;
		logger.debug(String.format("%s> close(%s)", this.id, result));	
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
	
	private void testInterrupt() throws InterruptedException {
		if(this.interrupted != null) {
			InterruptedException ex = this.interrupted;
			this.interrupted = null;
			throw ex;
		}
	}
	
	@Override
	public String toString() {
		return this.id;
	}
	
	
}
