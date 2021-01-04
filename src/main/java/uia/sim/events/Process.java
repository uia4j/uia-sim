package uia.sim.events;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Generator;
import uia.cor.Yield;
import uia.sim.Env;
import uia.sim.Event;

public class Process extends Event {
	
    private static final Logger logger = LogManager.getLogger(Process.class);

	private final Generator<Event> taskGen;
	
	private Event target;
	
	/**
	 * The constructor.
	 * 
	 * @param env The environment.
	 * @param eventId The event id.
	 * @param task The task to be executed.
	 */
	public Process(Env env, String eventId, Consumer<Yield<Event>> task) {
		super(env, eventId);
		this.taskGen = Yield.accept(eventId, task);
		this.target = new Initialize(this);
	}
	
	public boolean isAlive() {
		return !this.taskGen.isClosed();
	}
	/**
	 * Returns The event that the process is currently waiting for.
	 * 
	 * @return The event.
	 */
	public Event getTarget() {
		return this.target;
	}

	/**
	 * Resumes to execute the task.
	 * 
	 * @param by The event resumes the process.
	 */
	synchronized void resume(Event by) {
		String tx = UUID.randomUUID().toString().substring(0, 6);
		logger.debug(String.format("%s> resume(%s)", getId(), tx));
		if(!by.isOk()) {
			this.target = null;
			this.taskGen.close();
			setValue(null);
			logger.debug(String.format("%s> resume(%s), terminated", getId(), tx));
			return;
		}

		this.env.setActiveProcess(this);
		
		// run next task.
		if(this.taskGen.next()) {
			final Event nextEvent = taskGen.getValue();
			nextEvent.addCallable(this::resume);
			this.target = nextEvent;
			logger.debug(String.format("%s> resume(%s), done", getId(), tx));
		}
		else {
			this.target = null;
			logger.debug(String.format("%s> resume(%s), closed", getId(), tx));
		}
		
		if(this.taskGen.isClosed()) {
			setValue(null);
		}
		this.env.setActiveProcess(null);
	}
	
	/**
	 * Interrupts this process.
	 * 
	 * @param th The case instance to interrupt this process.
	 */
	synchronized void interrupt(Throwable th) {
		logger.debug(String.format("%s> interrupt", getId()));
		Interruption.shceule(this, th);
	}
	
	@Override
	public String toString() {
		return getId();
	}
}
