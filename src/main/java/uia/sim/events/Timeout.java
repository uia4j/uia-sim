package uia.sim.events;

import uia.sim.Env;
import uia.sim.Event;

public class Timeout extends Event {

	private final int delay;
	
	/**
	 * The constructor <b>schedule</b> itself for processing by the environment automatically.
	 * 
	 * @param env
	 * @param delay
	 */
	public Timeout(Env env, int delay) {
		this(env, delay, null);
	}
	
	/**
	 * The constructor <b>schedule</b> itself for processing by the environment automatically.
	 * @param env
	 * @param delay
	 * @param value
	 */
	public Timeout(Env env, int delay, Object value) {
		super(env, "Timeout", value);
		this.delay = delay;
		env.schedule(this, PriorityType.NORMAL, delay);
	}

	@Override
	public String toString() {
		return "Timeout(" + this.delay + ")";
	}
}
