package uia.sim;

public class Timeout extends Event {

	private final int delay;
	
	public Timeout(Env env, int delay) {
		super(env, "Timeout");
		this.delay = delay;
		env.schedule(this, PriorityType.NORMAL, delay);
	}
	
	public int getNow() {
		return this.env.getNow() + delay;
	}

	@Override
	public String toString() {
		return "Timeout:" + this.delay;
	}
}
