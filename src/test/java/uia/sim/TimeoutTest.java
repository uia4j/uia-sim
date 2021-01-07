package uia.sim;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.events.Process;
import uia.sim.events.Timeout;

public class TimeoutTest {

	@Test
	public void testDiscreteTimeStep() {
		Env env = new Env();
		ArrayList<Integer> logs = new ArrayList<>();
		env.process("timeout", y -> {
			int i = 0;
			try {
				while(true) {
					y.call(env.timeout(1));
					logs.add(i++);
				}
			}
			catch(Exception ex) {
				System.out.println(ex);
			}
		});
		env.run(3);
		System.out.println(logs);	// ?
		Assert.assertEquals(3, logs.size());
		Assert.assertArrayEquals(new Integer[] {0, 1, 2}, logs.toArray(new Integer[0]));
	}

	@Test
	public void testValue() {
		Env env = new Env();
		env.process("timeout", y -> {
			try {
				Object ok = y.call(env.timeout(1, "ok"));
				Assert.assertEquals("ok", ok);
			} catch (Exception e) {
				Assert.assertTrue(false);
			}
		});
		env.run();
	}

	@Test
	public void testShared() {
		Env env = new Env();
		final Timeout timeout = env.timeout(1);
		final ArrayList<String> logs = new ArrayList<>();
		env.process("t1", y -> {
			try {
				y.call(timeout);
				logs.add("t1");
			} catch (Exception e) {
				Assert.assertTrue(false);
			}
		});
		env.process("t2", y -> {
			try {
				y.call(timeout);
				logs.add("t2");
			} catch (Exception e) {
				Assert.assertTrue(false);
			}
		});
		env.process("t3", y -> {
			try {
				y.call(timeout);
				logs.add("t3");
			} catch (Exception e) {
				Assert.assertTrue(false);
			}
		});
		env.run();
		System.out.println(logs);
	}
	

	@Test
	public void testTrigger() {
		Env env = new Env();
		env.process("parent", y -> {
			try {
				y.call(env.timeout(2));
				final Process child = env.process("child", y2 -> {
					try {
						Object value = y2.call(env.timeout(1, "I was already done"));
						y2.close(value);	// not necessary
					}
					catch(Exception ex) {
						
					}
				});
				Object value = y.call(child);
				Assert.assertEquals("I was already done", value);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		});
		
		
		env.run();
	}
	
}
