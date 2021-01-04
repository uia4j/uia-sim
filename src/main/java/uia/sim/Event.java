package uia.sim;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Event {
	
    private static final Logger logger = LogManager.getLogger(Event.class);
	
	public static final Object PENDING = new Object();
	
	public enum PriorityType {
		URGENT,
		NORMAL
	}
	
	protected final Env env;
	
	protected final String id;
	
	protected Object value;
	
	private boolean ok;
	
	private boolean defused;
	
	private ArrayList<Consumer<Event>> callables;
	
	public Event(Env env, String id) {
		this(env, id, PENDING);
	}
	
	public Event(Env env, String id, Object value) {
		this.env = env;
		this.id = id;
		this.value = value;
		this.ok = true;
		this.callables = new ArrayList<>(); 
	}
	
	public Env getEnv() {
		return this.env;
	}
	
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

	public int getCallbablesCount() {
		return this.callables.size();
	}
	
	public boolean addCallable(Consumer<Event> callable) {
		return this.callables.add(callable);
	}
	
	public boolean removeCallable(Consumer<Event> callable) {
		return this.callables.remove(callable);
	}
	
	/**
	 * Becomes true if the event has been triggered and it's callables are about to be invoked.
	 * 
	 * @return Triggered or not.
	 */
	public boolean isTriggered() {
		return this.value != PENDING;
	}
	
	public boolean isProcessed() {
		return this.callables.isEmpty();
	}
	
	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean isDefused() {
		return this.defused;
	}

	public void setDefused(boolean defused) {
		this.defused = defused;
	}
	
	public synchronized void trigger(Event event) {
		logger.debug(String.format("%s> trigger", getId()));
		this.ok = event.isOk();
		this.value = event.getValue();
		this.env.schedule(this, PriorityType.NORMAL);
	}
	
	public synchronized void succeed(Object value) {
		logger.debug(String.format("%s> succeed", getId()));
		if(this.value != PENDING) {
			throw new IllegalStateException("The event:" + this.id + " has alreday been triggered");
		}

		this.ok = true;
		this.value = value;
		this.env.schedule(this, PriorityType.NORMAL);
	}
	
	public synchronized void fail(Throwable th) {
		logger.debug(String.format("%s> fail", getId()));
		if(this.value != PENDING) {
			throw new IllegalStateException("The event:" + this.id + " has alreday been triggered");
		}
		
		this.ok = false;
		this.value = th;
		this.env.schedule(this, PriorityType.NORMAL);
	}

	/**
	 * Executes instances of callable then remove them.
	 * 
	 */
	public synchronized void terminate() {
		setOk(false);
		while(!callables.isEmpty()) {
			callables.remove(0).accept(this);
		}
	}

	/**
	 * Executes instances of callable then remove them.
	 * 
	 */
	public synchronized void callback() {
		while(!callables.isEmpty()) {
			callables.remove(0).accept(this);
		}
	}

	@Override
	public String toString() {
		return getId();
	}
}
