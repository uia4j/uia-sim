package uia.sim;

public class RealtimeEnv extends Env {
	
	private long startTime;
	
	private int tickSize;
	
	public RealtimeEnv() {
		this.tickSize = 1000;
	}
	
	/**
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
		Job job = this.jobs.get(0);
		long triggerTime = this.startTime + (job.time - this.initialTime) * this.tickSize;
		try {
			Thread.sleep(Math.max(0, triggerTime - System.currentTimeMillis()));
		} catch (InterruptedException e) {

		}
		super.step();
	}
}
