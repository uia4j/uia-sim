package uia.sim;

import uia.sim.events.Process;

public class InterruptException extends SimException {

	/**
	 * 
	 */ 
	private static final long serialVersionUID = -2067294123704844987L;

	public InterruptException(Process process, Throwable th) {
		super(process, th);
	}

}
