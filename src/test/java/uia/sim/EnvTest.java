package uia.sim;

import org.junit.Test;

import uia.cor.Yield;
import uia.sim.Env;
import uia.sim.Event;

public class EnvTest {
	
	@Test
	public void test1() throws Exception {
		Env env = new Env();
		env.process("clock", yield -> clock1(env, yield));
		System.out.println("now, name> event");
		System.out.println("----------------");
		env.run(26);
	}

	public void clock1(Env env, Yield<Event> yield) {
		while(true) {
			System.out.println(String.format("%3d, run1> in", env.getNow()));
			yield.call(env.timeout(2));
			System.out.println(String.format("%3d, run1> out", env.getNow()));
			yield.call(env.timeout(1));
		}
	}
	
	@Test
	public void test2() throws Exception {
		Env env = new Env();
		env.process("clock1", yield -> clock1(env, yield));
		env.process("clock2", yield -> clock2(env, yield));
		System.out.println("--- test2 ---");
		env.run(30);
	}
	
	public void clock2(Env env, Yield<Event> yield) {
		while(true) {
			System.out.println(String.format("%3d, run2> in", env.getNow()));
			yield.call(env.timeout(5));
			System.out.println(String.format("%3d, run2> out", env.getNow()));
			yield.call(env.timeout(2));
		}
	}
}
 