package uia.sim.events;

import uia.sim.Event;
import uia.sim.SimException;

/**
 * Immediately schedules an InterruptException with the given Throwable to be thrown into the process.

 * 
 * This event is automatically triggered when it is created.
 *
 * @author Kan
 *
 */
public class Interruption extends Event {

	private final Process process;
	
	public static Interruption schedule(Process process, Exception cause) {
		return schedule(process, new SimException(process, cause));
	}

	public static Interruption schedule(Process process, String cause) {
		if(process.getEnv().getActiveProcess() == process) {
            throw new RuntimeException("The process is not allowed to interrupt itself.");
		}
		if(process.isTriggered()) {
			throw new RuntimeException(process + " has terminated and cannot be interrupted.");
		}
		return new Interruption(process, new SimException(process, cause));
	}
	
	private Interruption(Process process, Exception cause) {
		super(process.getEnv(), "Interruption", cause);
		this.process = process;
		addCallable(this::interrupt);
		ng();
		defused();

		// schedule a event to interrupt the process.
		env.schedule(this, PriorityType.URGENT);
	}
	
	private void interrupt(Event event) {
		if(this.process.isTriggered()) {
			return;
		}

		Event target = this.process.getTarget();
		this.process.unbind(target);
		this.process.resume(this);
	}
	
	@Override
	public String toString() {
		return "INT(" + this.process.getId() +")";
	}
	
	
}
