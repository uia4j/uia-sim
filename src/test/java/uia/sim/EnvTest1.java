package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.events.Process;

public class EnvTest1 {
	
	@Test
	public void test1() throws Exception {
		System.out.println("now, name> event");
		System.out.println("----------------");

		Env env = new Env();
		Process process = env.process("clock", yield -> clock1(env, yield));
		Assert.assertTrue(process.isAlive());
		Assert.assertEquals(26, env.run(26));
		Assert.assertFalse(process.isAlive());
	}
	
	@Test
	public void test2() throws Exception {
		System.out.println("now, name> event");
		System.out.println("----------------");

		Env env = new Env();
		Process process1 = env.process("clock1", yield -> clock1(env, yield));
		Process process2 = env.process("clock2", yield -> clock2(env, yield));
		Assert.assertTrue(process1.isAlive());
		Assert.assertTrue(process2.isAlive());
		env.run(30);
		Assert.assertFalse(process1.isAlive());
		Assert.assertFalse(process2.isAlive());
	}

	public void clock1(Env env, Yield2Way<Event, Object> yield) {
		while(!yield.isClosed()) {
			System.out.println(String.format("%3d, run1> in", env.getNow()));
			yield.call(env.timeout(2));
			System.out.println(String.format("%3d, run1> out", env.getNow()));
			yield.call(env.timeout(1));
		}
	}
	
	public void clock2(Env env, Yield2Way<Event, Object> yield) {
		while(!yield.isClosed()) {
			System.out.println(String.format("%3d, run2> in", env.getNow()));
			yield.call(env.timeout(5));
			System.out.println(String.format("%3d, run2> out", env.getNow()));
			yield.call(env.timeout(2));
		}
	}
}
 