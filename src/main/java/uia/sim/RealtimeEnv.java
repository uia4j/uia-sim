package uia.sim;

/**
 * The realtime environment.
 * 
 * @author Kan
 *
 */
public class RealtimeEnv extends Env {
	
	private long startTime;
	
	private int tickSize;
	
	/**
	 * The constructor.
	 */
	public RealtimeEnv() {
		this.tickSize = 1000;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param tickSize The microseconds of a tick. The minimum is 20ms.
	 */
	public RealtimeEnv(int tickSize) {
		this.tickSize = Math.max(tickSize, 20);
	}
	
	@Override
	public int run() {
		this.startTime = System.currentTimeMillis();
		return super.run();
	}

	@Override
	public int run(int until) {
		this.startTime = System.currentTimeMillis();
		return super.run(until);
	}

	@Override
	protected void step() throws SimException {
		Job job = this.jobs.peek();
		// Job job = this.jobs.get(0);
		long triggerTime = this.startTime + (job.time - this.initialTime) * this.tickSize;
		try {
			Thread.sleep(Math.max(0, triggerTime - System.currentTimeMillis()));
		} catch (InterruptedException e) {

		}
		super.step();
	}
}
