package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.cor.Yield2Way;
import uia.sim.Env;

public class ConditionTest {
	
	@Test
	public void testAnd() throws Exception {
		final Env env = new Env();
		env.process("testAnd", y -> and(env, y));
		env.run();
	}

	@Test
	public void testAndOr() throws Exception {
		final Env env = new Env();
		env.setListener(new EnvListenerAdapter());
		env.process("testAndOr", y -> andOr(env, y));
		env.run();
	}
	
	@Test
	public void testOr() throws Exception {
		final Env env = new Env();
		env.process("testOr", y -> or(env, y));
		env.run();
	}

	@Test
	public void testOrAnd() throws Exception {
		final Env env = new Env();
		env.setListener(new EnvListenerAdapter());
		env.process("testOrAnd", y -> orAnd(env, y));
		env.run();
	}

	public void and(Env env, Yield2Way<Event, Object> yield) {
		yield.call(env.timeout(10).and("and2", env.timeout(20)).and("and3", env.timeout(30)));
		System.out.println(env.getNow() + ", testAnd done");
		Assert.assertEquals(30, env.getNow());
	}

	public void andOr(Env env, Yield2Way<Event, Object> yield) {
		Event event1 = env.timeout(10).and("and1", env.timeout(30));
		Event event2 = env.timeout(15).and("and2", env.timeout(20));
		yield.call(event1.or("andOr", event2));
		Assert.assertEquals(20, env.getNow());
	}

	public void or(Env env, Yield2Way<Event, Object> yield) {
		yield.call(env.timeout(30).or("or2", env.timeout(20)).or("or3", env.timeout(10)));
		System.out.println(env.getNow() + ", testOr done");
		Assert.assertEquals(10, env.getNow());
	}

	public void orAnd(Env env, Yield2Way<Event, Object> yield) {
		Event event1 = env.timeout(10).or("and1", env.timeout(30));
		Event event2 = env.timeout(15).or("and2", env.timeout(20));
		yield.call(event1.or("andOr", event2));
		Assert.assertEquals(15, env.getNow());
	}
}
 