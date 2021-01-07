package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.events.Process;
import uia.sim.events.Timeout;

public class ProcessTest {

	@Test
	public void testState() {
		final Env env = new Env();
		final Process p1 = env.process("p1", y1 -> {
			try {
				y1.call(env.timeout(5));
			} catch (Exception e) {

			}
		});
		env.process("p2", y1 -> {
			try {
				Assert.assertTrue(p1.isAlive());
				y1.call(env.timeout(10));
				Assert.assertFalse(p1.isAlive());
			} catch (Exception e) {

			}
			
		});
		env.run();
	}

	@Test
	public void testTarget() {
		final Env env = new Env();
		final Event event = new Timeout(env, 10);
		
		Process proc = env.process("target",y -> {
			try {
				y.call(event);
			} catch (Exception e) {

			}
		});
		env.run();
		Assert.assertTrue(proc.getTarget() == event);
	}

	@Test
	public void testWaitForProc() {
		final Env env = new Env();
		env.process("waiter", y1 -> {
			try {
				y1.call(env.process("finisher",y2 -> {
					try {
						y2.call(env.timeout(10));
					} catch (Exception e) {
						e.printStackTrace();
						Assert.assertTrue(false);
					}
				}));
				Assert.assertEquals(10, env.getNow());
			} catch (Exception e) {
				e.printStackTrace();
				Assert.assertTrue(false);
			}
		});
		env.run();
	}

	@Test
	public void testReturnValue() {
		final Env env = new Env();
		env.process("bar", y1 -> {
			try {
				Process waiter = env.process("waiter",y2 -> {
					try {
						y2.call(env.timeout(10));
						y2.close("IPA-" + env.getNow());
					} catch (Exception e) {
						e.printStackTrace();
						Assert.assertTrue(false);
					}
				});
				Object beer1 = y1.call(waiter);
				Assert.assertEquals("IPA-10", beer1);
				
			} catch (Exception e) {
				e.printStackTrace();
				Assert.assertTrue(false);
			}
		});
		env.run();
	}


	@Test
	public void testInterruptAndJoin() {
		final Env env = new Env();
		final Process parent = env.process("parent", y1 -> {
			Process child = env.process("child",y2 -> {
				try {
					y2.call(env.timeout(10));
				} catch (Exception e) {
					Assert.assertTrue(false);
				} 
			});
			
			try {
				y1.call(child);
				Assert.assertTrue(false);
			} catch (Exception e1) {
				Assert.assertTrue(child.isAlive());
				Assert.assertEquals("down", e1.getMessage());
				Assert.assertEquals(1, env.getNow());
				
				try {
					y1.call(env.timeout(20));
					Assert.assertEquals(21, env.getNow());
				} catch (Exception e2) {
					Assert.assertTrue(false);
				}
			}
		});
		
		env.process("interuptor", y -> {
			try {
				y.call(env.timeout(1));
				parent.interrupt("down");
			} catch (Exception e) {

			}
		});
		
		env.run();
	}

	@Test
	public void testInterruptAndRejoin() {
		final Env env = new Env();
		final Process parent = env.process("parent", y1 -> {
			Process child = env.process("child",y2 -> {
				try {
					y2.call(env.timeout(10));
				} catch (Exception e) {
					Assert.assertTrue(false);
				}
			});
			
			try {
				y1.call(child);
				Assert.assertTrue(false);
			} catch (Exception e1) {
				Assert.assertTrue(child.isAlive());
				Assert.assertEquals("down", e1.getMessage());
				Assert.assertEquals(1, env.getNow());

				try {
					y1.call(child);
					Assert.assertEquals(10, env.getNow());
				} catch (Exception e2) {
					Assert.assertTrue(false);
				}
			}
		});
		
		env.process("interuptor", y -> {
			try {
				y.call(env.timeout(1));
				parent.interrupt("down");
			} catch (Exception e) {

			}
		});
		
		env.run();
	}
}
