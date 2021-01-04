package uia.sim.events;

import uia.sim.Event;

public class Initialize extends Event {

	private final Process process;
	
	public Initialize(Process process) {
		super(process.getEnv(), "Initialize", null);
		this.process = process;
		addCallable(this.process::resume);
		setOk(true);
		
		env.schedule(this, PriorityType.URGENT);
	}
	
	@Override
	public String toString() {
		return "Initialize:" + this.process.getId();
	}
}
