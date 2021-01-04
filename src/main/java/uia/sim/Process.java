package uia.sim;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Generator;
import uia.cor.Yield;

public class Process extends Event {
	
    private static final Logger logger = LogManager.getLogger(Process.class);

	private final Generator<Event> gen;
	
	private Event target;
	
	public Process(Env env, String name, Consumer<Yield<Event>> consumer) {
		super(env, name);
		this.target = new Initialize(env, this);
		this.gen = Yield.accept(name, consumer);
	}
	
	/**
	 * Returns The event that the process is currently waiting for.
	 * 
	 * @return The event.
	 */
	public Event getTarget() {
		return this.target;
	}

	synchronized void resume() {
		logger.debug(String.format("%s> resume", getId()));
		this.env.setActiveProcess(this);
		
		if(this.gen.next()) {
			final Event event = gen.getValue();
			event.addCallable(this::callResume);
			logger.debug(String.format("%s> resume, next() done", getId()));

			this.target = event;
		}
		else {
			this.target = null;
			logger.debug(String.format("%s> resume, next()=false", getId()));
		}
		this.env.setActiveProcess(null);
	}
	
	void callResume(Event evt) {
		resume();
	}
	
	@Override
	public String toString() {
		return getId();
	}
}
