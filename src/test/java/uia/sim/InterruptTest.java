package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.events.Process;

public class InterruptTest {

    @Test
    public void testInterrput() {
        final Env env = new Env();
        env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                try {
                    y2.call(env.timeout(100));
                    Assert.assertTrue(false);
                }
                catch (Exception e) {
                    Assert.assertEquals("parent interrupts the child", e.getMessage());
                    Assert.assertEquals(50, env.getNow());
                }
            });

            y1.call(env.timeout(50));
            child.interrupt("parent interrupts the child");
            Assert.assertEquals(50, env.getNow());
        });
        env.run();
    }

    public void testConcurrentInterrputs() {
        final Env env = new Env();
        final Process fox = env.process("fox", y1 -> {
            while (true) {
                try {
                    y1.call(env.timeout(10));
                }
                catch (Exception e) {
                    System.out.println(env.getNow() + "> fox> " + e.getMessage());
                }
            }
        });

        env.process("boggis", y2 -> {
            y2.call(env.timeout(2));
            fox.interrupt("boggis");
        });
        env.process("bunce", y -> {
            y.call(env.timeout(3));
            fox.interrupt("bunce");
        });
        env.process("beans", y -> {
            y.call(env.timeout(5));
            fox.interrupt("beans");
        });

        env.run(40);
    }

    @Test
    public void testInitInterrput() {
        final Env env = new Env();
        env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                try {
                    y2.call(env.timeout(10));
                    Assert.assertTrue(false);
                }
                catch (Exception e) {
                    Assert.assertEquals(0, env.getNow());
                    Assert.assertEquals("initial interrput", e.getMessage());
                }
            });
            child.interrupt("initial interrput");
            y1.call(env.timeout(1));
        });
        env.run(40);
    }

    @Test
    public void testInterrputTerminatedProcess() {
        final Env env = new Env();
        env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                try {
                    y2.call(env.timeout(5));
                }
                catch (Exception e) {
                }
            });
            try {
                // wait long enough so that child terminates.
                y1.call(env.timeout(10));
                child.interrupt("too late to interrput");
                Assert.assertTrue(false);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        env.run(40);
    }

    @Test
    public void testMultipleInturrupts() {
        final Env env = new Env();
        env.process("boss", y1 -> {
            Process employee = env.process("employee", y2 -> {
                try {
                    y2.call(env.timeout(4));
                }
                catch (Exception e) {
                    y2.close(e.getMessage() + " - no more work");
                }
            });

            y1.call(env.timeout(1));
            employee.interrupt("happy hour");
            employee.interrupt("back to work");
            Object value = y1.call(employee);
            Assert.assertEquals("happy hour - no more work", value);
            y1.call(env.timeout(2));
            employee.interrupt("back to work, please");
        });
        env.run(40);
    }

    @Test
    public void testInterrputEvent() {
        final Env env = new Env();
        env.process("parent", y1 -> {
            Process child = env.process("child", y2 -> {
                try {
                    y2.call(env.event("playing"));
                }
                catch (Exception e) {
                    Assert.assertEquals(10, env.getNow());
                    System.out.println(e.getMessage());
                }
            });

            y1.call(env.timeout(10));
            child.interrupt("interrput a event");
        });
        env.run(40);
    }

    @Test
    public void testInterrputSelf() {
        final Env env = new Env();
        env.process("parent", y1 -> {
            try {
                y1.call(env.timeout(10));
                env.getActiveProcess().interrupt("not allow to interrput self");
                y1.call(env.timeout(10));
            }
            catch (Exception e) {
                Assert.assertEquals(10, env.getNow());
                System.out.println(e.getMessage());
            }
        });
        env.run(40);
    }
}
