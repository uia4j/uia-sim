package uia.sim;

import org.junit.Assert;
import org.junit.Test;

import uia.cor.Yield2Way;
import uia.cor.Yieldable2Way;
import uia.sim.events.Process;

public class EnvTest {

    @Test
    public void testHello() {
        Env env = new Env();
        env.process(new Hello("Jack"));
        env.run();
    }

    @Test
    public void testBye() {
        Env env = new Env();
        env.process("bye", new Bye(env));
        env.run();
    }

    @Test
    public void testListener() throws InterruptedException {
        final Env env = new Env();
        env.setListener(new EnvListenerAdapter());
        env.process("main", y -> {
            int i = 1;
            while (true) {
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

            final Process sub = env.process("pg2", y2 -> {
                try {
                    y2.call(env.timeout("pg2-1", 7));
                    y2.call(env.timeout("pg2-2", 7));
                    Assert.assertTrue(false);
                }
                catch (Exception ex) {
                    Assert.assertEquals("pg1-3", ex.getMessage());

                }
                y2.call(env.timeout("pg2-3", 2));
            });

            int i = 1;
            while (true) {
                y1.call(env.timeout("pg1-" + i, 3));
                if (i == 3) {
                    sub.interrupt("pg1-" + i);
                }
                i++;
            }
        });
        env.run(30);
    }

    @Test
    public void testProcess1() throws Exception {
        System.out.println("now, name> event");
        System.out.println("----------------");

        Env env = new Env();
        Process process = env.process("clock", yield -> clock1(env, yield));
        Assert.assertTrue(process.isAlive());
        Assert.assertEquals(16, env.run(16));
        Assert.assertFalse(process.isAlive());
    }

    @Test
    public void testProcess2() throws Exception {
        System.out.println("now, name> event");
        System.out.println("----------------");

        Env env = new Env();
        Process process1 = env.process("clock1", yield -> clock1(env, yield));
        Process process2 = env.process("clock2", yield -> clock2(env, yield));
        Assert.assertTrue(process1.isAlive());
        Assert.assertTrue(process2.isAlive());
        env.run(10);
        Assert.assertFalse(process1.isAlive());
        Assert.assertFalse(process2.isAlive());
    }

    @Test
    public void testProcess3() throws Exception {
        System.out.println("now, name> event");
        System.out.println("----------------");

        Env env = new Env(10);
        Process process = env.process("clock", yield -> clock1(env, yield));
        Assert.assertTrue(process.isAlive());
        Assert.assertEquals(14, env.run(14));
        Assert.assertFalse(process.isAlive());
    }

    public void clock1(Env env, Yield2Way<Event, Object> yield) {
        while (!yield.isClosed()) {
            System.out.println(String.format("%3d, run1> in", env.getNow()));
            yield.call(env.timeout(2));
            System.out.println(String.format("%3d, run1> out", env.getNow()));
            yield.call(env.timeout(1));
        }
    }

    public void clock2(Env env, Yield2Way<Event, Object> yield) {
        while (!yield.isClosed()) {
            System.out.println(String.format("%3d, run2> in", env.getNow()));
            yield.call(env.timeout(5));
            System.out.println(String.format("%3d, run2> out", env.getNow()));
            yield.call(env.timeout(2));
        }
    }

    /**
     * Extends the Processable to implement a process.
     *
     * @author Kan
     *
     */
    public class Hello extends Processable {

        public Hello(String name) {
            super(name);
        }

        @Override
        public void run() {
            yield(env().timeout(10));
            System.out.println(now() + "> Hello " + getId());
            yield().call(env().timeout(10));
            System.out.println(now() + "> Hi " + getId());
        }
    }

    /**
     * Extends the Yieldable2Way to implement a process.
     *
     * @author Kan
     *
     */
    public class Bye extends Yieldable2Way<Event, Object> {

        private final Env env;

        public Bye(Env env) {
            this.env = env;
        }

        @Override
        protected void run() {
            yield(this.env.timeout(10));
            System.out.println(this.env.getNow() + "> Bye");
            yield().call(this.env.timeout(10));
            System.out.println(this.env.getNow() + "> See you");
        }
    }
}
