package uia.sim.events;

import uia.sim.Event;

public class Initialize extends Event {

	private final Process process;
	
	public Initialize(Process process) {
		super(process.getEnv(), "Initialize", null);
		this.process = process;
		this.process.bind(this);
		// addCallable(this.process::resume);
		
		env.schedule(this, PriorityType.URGENT);
	}
	
	@Override
	public String toString() {
		return "Init(" + this.process.getId() +")";
	}
}
