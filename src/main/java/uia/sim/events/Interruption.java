package uia.sim.events;

import uia.sim.Event;
import uia.sim.InterruptException;

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
		return new Interruption(process, cause);
	}
	
	private Interruption(Process process, Exception cause) {
		super(process.getEnv(), "Interruption", new InterruptException(process, cause));
		this.process = process;
		addCallable(this::interrupt);
		ng();
		defused();

		env.schedule(this, PriorityType.URGENT);
	}
	
	private void interrupt(Event event) {
		this.process.getTarget().removeCallable(this.process::resume);
		this.process.resume(this);
	}

	@Override
	public String toString() {
		return "Interruption:" + this.process.getId();
	}
	
	
}
