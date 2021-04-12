package uia.sim;

import org.junit.Test;

import uia.cor.Yield2Way;

public class WaitingTest {

	private Env env;
	
	private Event waiting;
	
	@Test
	public void test() {
        this.env = new Env();
        this.env.process("waiting", this::waiting);
        this.env.process("release", this::release);
        this.env.run();
    }
	
	private void waiting(Yield2Way<Event, Object> yield) {
		this.waiting = this.env.event("waiting");
		yield.call(this.waiting);
		System.out.println(this.env.getNow());

		this.waiting = this.env.event("waiting");
		yield.call(this.waiting);
		System.out.println(this.env.getNow());
}
	
	private void release(Yield2Way<Event, Object> yield) {
		yield.call(this.env.timeout(100));
		this.waiting.succeed(null);

		yield.call(this.env.timeout(300));
		this.waiting.succeed(null);
	}
}
