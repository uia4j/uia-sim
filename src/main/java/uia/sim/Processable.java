package uia.sim;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;
import uia.sim.events.Process;

public abstract class Processable {
	
	private final String id;

	private Env env;

	private Process process;

	private Yield2Way<Event, Object> yield;
	
	protected Processable(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public final Process bind(Env env) {
		if(this.env != null) {
			throw new RuntimeException("has binded already");
		}
		
		this.env = env;
		this.process = env.process(id, this::readyToGo);
		return this.process;
	}
	
	public final void stop() {
		if(this.yield != null) {
			this.yield.close();
		}
	}
	
	protected final Env env() {
		return this.env;
	}

	protected final Process proc() {
		return this.process;
	}

	protected final Yield2Way<Event, Object> yield() {
		return this.yield;
	}
	
	protected final Object yield(Event event) {
		return yield.call(event);
	}
	
	protected abstract void run();
	
	private void readyToGo(Yield2Way<Event, Object> yield) {
		this.yield = yield;
		run();
	}
}
