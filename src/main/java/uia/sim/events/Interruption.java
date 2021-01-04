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
	
	public static void shceule(Process process, Throwable th) {
		new Interruption(process, th);
	}
	
	private Interruption(Process process, Throwable th) {
		super(process.getEnv(), "Interruption", new InterruptException(process, th));
		this.process = process;
		addCallable(this::interrupt);
		setOk(false);
		setDefused(true);

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
