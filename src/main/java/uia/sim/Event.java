package uia.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.events.AllOf;
import uia.sim.events.AnyOf;

public class Event {
	
    private static final Logger logger = LogManager.getLogger(Event.class);
	
	private static final Object PENDING = new Object();
	
	public enum PriorityType {
		
		URGENT(0),
		
		HIGH(1),

		NORMAL(2),
		
		LOW(3);
		
		public final int level;
		
		PriorityType(int level) {
			this.level = level;
		}
	}
	
	protected final Env env;
	
	protected final String id;
	
	private Object value;
	
	private boolean envDown;
	
	private boolean ok;
	
	private boolean processed;
	
	private boolean defused;
	
	private ArrayList<Consumer<Event>> callables;
	
	/**
	 * The constructor.
	 * 
	 * @param env The environment.
	 * @param id The id.
	 */
	public Event(Env env, String id) {
		this(env, id, PENDING);
	}

	/**
	 * The constructor.
	 * 
	 * @param env The environment.
	 * @param id The id.
	 * @param value The value of the event.
	 */
	public Event(Env env, String id, Object value) {
		this.env = env;
		this.id = id;
		this.value = value;
		this.envDown = false;
		this.ok = true;
		this.processed = false;
		this.defused = false;
		this.callables = new ArrayList<>(); 
	}
	
	/**
	 * Returns the environment of the event.
	 * 
	 * @return The environment.
	 */
	public Env getEnv() {
		return this.env;
	}
	
	/**
	 * Returns the id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return this.id;
	}
	
	public Object getValue() {
		//if(this.value == PENDING) {
		//	throw new IllegalStateException("Value of event:" + this.id + " is not yet available");
		//}
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the number of callable instances.
	 * @return
	 */
	public int getNumberOfCallbables() {
		return this.callables.size();
	}
	
	/**
	 * Adds a callable instance which will be executed at the scheduled time.
	 * 
	 * @param callable A callable instance.
	 * @return Successful added or not.
	 */
	public boolean addCallable(Consumer<Event> callable) {
		return this.callables.add(callable);
	}
	
	/**
	 * Removes a callable instance.
	 * 
	 * @param callable A callable instance.
	 * @return Successful removed or not.
	 */
	public boolean removeCallable(Consumer<Event> callable) {
		return this.callables.remove(callable);
	}

	/**
	 * AND with other event.
	 * 
	 * @param other The other event.
	 * @return A new condition event.
	 */
	public Event and(Event other) {
		return new AllOf(env, Arrays.asList(this, other));
	}
	
	/**
	 * OR with other event.
	 * 
	 * @param other The other event.
	 * @return A new condition event.
	 */
	public Event or(Event other) {
		return new AnyOf(env, Arrays.asList(this, other));
	}

	public boolean isEnvDown() {
		return envDown;
	}

	public void envDown() {
		this.ok = false;
		this.envDown = true;
		callback();
	}

	/**
	 * Tests if the event has been triggered and it's callables are about to be invoked.
	 * 
	 * @return Triggered or not.
	 */
	public boolean isTriggered() {
		return this.value != PENDING;
	}
	
	/**
	 * Tests if the event has been processed.
	 * 
	 * @return Processed or not.
	 */
	public boolean isProcessed() {
		return this.processed;
	}
	
	/**
	 * Tests if the event has been triggered successfully.
	 * 
	 * @return OK or not.
	 */
	public boolean isOk() {
		return this.ok;
	}

	/**
	 * Tests if failed event's exception has been defused or not.<br>
     *
     * When an event fails (i.e. calling method`fail()), the failed event's value 
     * is an exception that will be re-raised when the
     * environment processes the event (i.e. in environment method::step()`).
	 * 
     * It is also possible for the failed event's exception to be defused by 
     * setting defused property to true from an event callback. 
     * 
     * Doing so prevents the event's exception from being re-raised when the event is 
     * processed by the environment.
     * 
     * @return True when failed event's exception has been defused.
	 */
	public boolean isDefused() {
		return this.defused;
	}

	public void ng() {
		this.ok = false;
	}

	/**
	 * Defuses the event/
	 * 
	 */
	public void defused() {
		this.defused = true;
	}
	
	/**
	 * Updates the event with the state and value of the provided event and 
	 * <b>schedule</b> it for processing by the environment.
.	 * 
	 * @param event The specific event. 
	 */
	public synchronized void trigger(Event event) {
		logger.debug(String.format("%s> triggered by %s", getId(), event.getId()));
		this.ok = event.isOk();
		this.value = event.value;
		// schedule
		this.env.schedule(this, PriorityType.NORMAL);
	}
	
	/**
	 * Marks it as successful and 
	 * <b>schedule</b> it for processing by the environment.
	 * 
	 * @param value The value of the event.
	 */
	public synchronized void succeed(Object value) {
		logger.debug(String.format("%s> succeed", getId()));
		if(isTriggered()) {
			throw new RuntimeException("The event:" + this.id + " has alreday been triggered");
		}

		this.ok = true;
		this.value = value;
		// schedule
		this.env.schedule(this, PriorityType.NORMAL);
	}
	
	/**
	 * Marks the event as failed with a cause and 
	 * <b>schedule</b> it for processing by the environment.
	 * 
	 * @param cause The failed cause.
	 */
	public synchronized void fail(Exception cause) {
		logger.debug(String.format("%s> fail, %s", getId(), cause.getMessage()));
		if(isTriggered()) {
			throw new RuntimeException("The event:" + this.id + " has alreday been triggered");
		}
		
		this.ok = false;
		this.value = cause;
		// schedule
		this.env.schedule(this, PriorityType.NORMAL);
	}

	/**
	 * Executes all instances of callable.
	 * 
	 */
	public synchronized void callback() {
		try {
			while(!this.callables.isEmpty()) {
				this.callables.remove(0).accept(this);
			}
		}
		finally {
			this.processed = true;
			this.callables.clear();
		}
	}

	@Override
	public String toString() {
		return "E(" + getId() + ")";
	}

	public String toFullString() {
		return String.format("%s(ok=%s,triggered=%s,proccessed=%s)", 
				getId(),
				this.isOk(),
				this.isTriggered(),
				this.isProcessed());
	}
}