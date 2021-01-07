package uia.sim;

import org.junit.Test;

import uia.cor.Yield2Way;
import uia.sim.Env;

public class SchoolTest {
	
	private Env env;

	private Event classBegin;

	private Event classEnd;
	
	public SchoolTest() {
		this.env = new Env();
		this.classBegin = this.env.event("classBegin");
		this.classEnd = this.env.event("classEnd");
		env.process("pupil-1", this::pupil);
		env.process("pupil-2", this::pupil);
		env.process("pupil-3", this::pupil);
		env.process("bell", this::bell);
	}

	public void bell(Yield2Way<Event, Object> yield) {
		while(true) {
			this.classBegin.succeed(null);
			this.classBegin = this.env.event("classBegin");	// update event
			System.out.println(String.format("\n%3d> begin", this.env.getNow()));

			yield.call(env.timeout(45));
			this.classEnd.succeed(null);
			this.classEnd = this.env.event("classEnd");		// update event
			System.out.println(String.format("\n%3d> end", this.env.getNow()));

			yield.call(env.timeout(15));
		}
	}
	
	public void pupil(Yield2Way<Event, Object> yield) {
		while(true) {
			yield.call(this.classBegin);
			System.out.print("|O| ");
			// This call will make method:'resume' of the pupil process to be a callable of the classEnd event.
			// when classEnd is time up, pupil.resume will be invoked.
			yield.call(this.classEnd);
			System.out.print("\\o/ ");
		}
	}
	
	@Test
	public void test1() throws Exception {
		this.env.run(300);
	}
}
 