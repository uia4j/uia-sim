package uia.sim;

import org.junit.Test;

import uia.cor.Yield2Way;

public class RealtimeEnvTest {
	
	private RealtimeEnv env;

	private Event classEnd;
	
	public RealtimeEnvTest() {
		this.env = new RealtimeEnv(20);
		this.classEnd = this.env.event("classEnd");
		env.process("pupil-1", this::pupil);
		env.process("pupil-2", this::pupil);
		env.process("pupil-3", this::pupil);
		env.process("bell", this::bell);
	}

	public void bell(Yield2Way<Event, Object> yield) {
		try {
			while(yield.isAlive()) {
				yield.call(env.timeout(20));
				this.classEnd.succeed(null);
				this.classEnd = this.env.event("classEnd");
				System.out.println(String.format("\n%3d> bell is ringing...", this.env.getNow()));
			}
		} catch (Exception e) {
			System.out.println(yield.getId() + " interrupted");
		}
	}
	
	public void pupil(Yield2Way<Event, Object> yield) {
		try {
			while(yield.isAlive()) {
				// This call will make method:'resume' of the pupil process to be a callable of the classEnd event.
				// when classEnd is time up, pupil.resume will be invoked.
				yield.call(this.classEnd);
				System.out.print("\\o/ ");
			}
		} catch (Exception e) {
			System.out.println(yield.getId() + " interrupted");
		}
	}
	
	@Test
	public void test1() throws Exception {
		this.env.run(200);
	}
}
 