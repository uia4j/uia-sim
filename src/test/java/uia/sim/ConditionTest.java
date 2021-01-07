package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.cor.Yield2Way;
import uia.sim.Env;

public class ConditionTest {
	
	@Test
	public void testAnd() throws Exception {
		final Env env = new Env();
		env.process("test1", y -> and(env, y));
		env.run();
	}

	
	@Test
	public void testOr() throws Exception {
		final Env env = new Env();
		env.process("test1", y -> or(env, y));
		env.run();
	}

	public void and(Env env, Yield2Way<Event, Object> yield) {
		yield.call(env.timeout(10).and(env.timeout(20)));
		System.out.println(env.getNow() + ", testAnd done");
		Assert.assertEquals(20, env.getNow());
	}

	public void or(Env env, Yield2Way<Event, Object> yield) {
		yield.call(env.timeout(10).or(env.timeout(20)));
		System.out.println(env.getNow() + ", testOr done");
		Assert.assertEquals(10, env.getNow());
	}
}
 