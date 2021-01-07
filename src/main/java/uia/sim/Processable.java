package uia.sim;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;

public abstract class Processable {

	protected final Env env;
	
	protected final String id;
	
	protected Processable(Env env, String id) {
		this.env = env;
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public abstract void run(Yield2Way<Event, Object> yield);
	
	public abstract void stop();
}
