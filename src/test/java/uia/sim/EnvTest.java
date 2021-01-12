package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.events.Process;

public class EnvTest {
	
	@Test
	public void testHello() {
		Env env = new Env();
		env.process(new Hello("Jack"));
		env.run();
	}
	
	@Test
	public void testListener() {
		final Env env = new Env();
		env.setListener(new EnvListenerAdapter());
		env.process("main", y -> {
			int i = 1;
			while(true) {
				y.call(env.timeout("" + i++, 3));
			}
		});
		env.run(20);
	}

	
	@Test
	public void testListenerWithFailed() {
		final Env env = new Env();
		env.setListener(new EnvListenerAdapter());
		env.process("pg1", y1 -> {
			
			final Process sub = env.process("pg2" , y2 -> {
				try {
					y2.call(env.timeout("pg2-1", 7));
					y2.call(env.timeout("pg2-2", 7));
					Assert.assertTrue(false);
				}
				catch(Exception ex) {
					Assert.assertEquals("pg1-3", ex.getMessage());
					
				}
				y2.call(env.timeout("pg2-3", 2));
			});
			
			int i = 1;
			while(true) {
				y1.call(env.timeout("pg1-" + i, 3));
				if(i == 3) {
					sub.interrupt("pg1-" + i);
				}
				i++;
			}
		});
		env.run(30);
	}

	@Test
	public void test1Process() throws Exception {
		System.out.println("now, name> event");
		System.out.println("----------------");

		Env env = new Env();
		Process process = env.process("clock", yield -> clock1(env, yield));
		Assert.assertTrue(process.isAlive());
		Assert.assertEquals(26, env.run(26));
		Assert.assertFalse(process.isAlive());
	}
	
	@Test
	public void test2Processes() throws Exception {
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
	
	public class Hello extends Processable {
		
		public Hello(String name) {
			super(name);
		}

	    public void run() {
	        yield(env().timeout(10));
	        System.out.println(now() + "> Hello " + getId());
	    }
	}
}
 