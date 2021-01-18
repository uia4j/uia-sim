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
            y1.call(env.timeout(5));
        });
        env.process("p2", y1 -> {
            Assert.assertTrue(p1.isAlive());
            y1.call(env.timeout(10));
            Assert.assertFalse(p1.isAlive());
        });
        env.run();
    }

    @Test
    public void testTarget() {
        final Env env = new Env();
        final Event event = new Timeout(env, 10);

        Process proc = env.process("target", y -> {
            y.call(event);
        });
        env.run();
        Assert.assertTrue(proc.getTarget() == event);
    }

    @Test
    public void testWaitForProc() {
        final Env env = new Env();
        env.process("waiter", y1 -> {
            y1.call(env.process("finisher", y2 -> {
                y2.call(env.timeout(10));
            }));
            Assert.assertEquals(10, env.getNow());
        });
        env.run();
    }

    @Test
    public void testReturnValue() {
        final Env env = new Env();
        env.process("bar", y1 -> {
            Process waiter = env.process("waiter", y2 -> {
                y2.call(env.timeout(10));
                y2.close("IPA-" + env.getNow());
            });
            Object beer1 = y1.call(waiter);
            Assert.assertEquals("IPA-10", beer1);
        });
        env.run();
    }

    @Test
    public void testInterruptAndJoin() {
        final Env env = new Env();
        final Process parent = env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                y2.call(env.timeout(10));
            });

            try {
                y1.call(child);
                Assert.assertTrue(false);
            }
            catch (Exception e1) {
                Assert.assertTrue(child.isAlive());
                Assert.assertEquals("down", e1.getMessage());
                Assert.assertEquals(1, env.getNow());

                y1.call(env.timeout(20));
                Assert.assertEquals(21, env.getNow());
            }
        });

        env.process("interuptor", y -> {
            y.call(env.timeout(1));
            parent.interrupt("down");
        });

        env.run();
    }

    @Test
    public void testInterruptAndRejoin() {
        final Env env = new Env();
        final Process parent = env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                y2.call(env.timeout(10));
            });

            try {
                y1.call(child);
                Assert.assertTrue(false);
            }
            catch (Exception e1) {
                Assert.assertTrue(child.isAlive());
                Assert.assertEquals("down", e1.getMessage());
                Assert.assertEquals(1, env.getNow());

                y1.call(child);
                Assert.assertEquals(10, env.getNow());
            }
        });

        env.process("interuptor", y -> {
            y.call(env.timeout(1));
            parent.interrupt("down");
        });

        env.run();
    }
}
