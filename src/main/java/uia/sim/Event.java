package uia.sim;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Event {
	
	public enum PriorityType {
		URGENT,
		NORMAL
	}
	
	protected final Env env;
	
	private final String id;
	
	private boolean ok;
	
	private boolean defused;
	
	private Object value;
	
	private ArrayList<Consumer<Event>> callables;
	
	public Event(Env env, String id) {
		this.env = env;
		this.id = id;
		this.ok = true;
		this.callables = new ArrayList<>(); 
	}
	
	public String getId() {
		return this.id;
	}
	
	public int getNow() {
		return this.env.getNow();
	}
	
	public int countCallables() {
		return this.callables.size();
	}
	
	public void addCallable(Consumer<Event> callable) {
		this.callables.add(callable);
	}
	
	public synchronized void callback() {
		while(!callables.isEmpty()) {
			callables.remove(0).accept(this);
		}
	}
	
	public boolean isProcessed() {
		return this.callables.isEmpty();
	}
	
	public void trigger(Event event) {
		this.ok = true;
		this.value = event;
		this.env.schedule(this, PriorityType.NORMAL, 0);
	}
	
	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public boolean isDefused() {
		return defused;
	}

	public void setDefused(boolean defused) {
		this.defused = defused;
	}

	@Override
	public String toString() {
		return getId();
	}
	
	
}
