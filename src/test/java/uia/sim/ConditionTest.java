package uia.sim;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.events.AllOf;
import uia.sim.events.Condition;
import uia.sim.events.ConditionValue;
import uia.sim.events.Process;
import uia.sim.events.Timeout;

public class ConditionTest {

    @Test
    public void testEmpty() throws Exception {
        final Env env = new Env();
        env.setListener(new EnvListenerAdapter());
        env.process("empty", y -> {
            ConditionValue cv = (ConditionValue) y.call(new AllOf(env, "AllOf", Arrays.asList()));
            Assert.assertEquals(0, env.getNow());
            Assert.assertEquals(0, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testAnd() throws Exception {
        final Env env = new Env();
        env.process("testAnd", y -> {
            ConditionValue cv = (ConditionValue) y.call(
                    env.timeout("t1", 10)
                            .and(env.timeout("t2", 20))
                            .and(env.timeout("t3", 30)));
            Assert.assertEquals(30, env.getNow());
            Assert.assertEquals(3, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testOr() throws Exception {
        final Env env = new Env();
        env.process("testOr", y -> {
            ConditionValue cv = (ConditionValue) y.call(
                    env.timeout("t1", 30)
                            .or("OR1", env.timeout("t2", 10))
                            .or("OR2", env.timeout("t3", 20)));
            Assert.assertEquals(10, env.getNow());
            Assert.assertEquals(1, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testAndOr() throws Exception {
        final Env env = new Env();
        env.process("testAndOr", y -> {
            Condition event1 = env.timeout("t1", 10)    // v
                    .and(env.timeout("t2", 20))         // v
                    .and(env.timeout("t3", 30));
            Condition event2 = env.timeout("t4", 15)    // v
                    .and(env.timeout("t5", 25));        // v

            ConditionValue cv = (ConditionValue) y.call(event1.or("andOr", event2));
            Assert.assertEquals(25, env.getNow());
            Assert.assertEquals(4, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testOrAnd() throws Exception {
        final Env env = new Env();
        env.process("testOrAnd", y -> {
            Condition event1 = env.timeout(10)      // v
                    .or(env.timeout(20))            // v
                    .or(env.timeout(40));
            Condition event2 = env.timeout(25)      // v
                    .or(env.timeout(50));

            ConditionValue cv = (ConditionValue) y.call(event1.and("andOr", event2));
            Assert.assertEquals(25, env.getNow());
            Assert.assertEquals(3, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testInterupt() throws Exception {
        final Env env = new Env();
        Condition condition = env.timeout("t1", 10)
                .and("A1", env.timeout("t2", 20))
                .and("A2", env.timeout("t3", 30));
        Process p1 = env.process("testInterupt", y -> {
            try {
                y.call(condition);
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertTrue(true);
            }
        });

        env.process("interrupt", y -> {
            y.call(env.timeout(15));
            p1.interrupt("go");
            condition.ng();             // NG
            y.call(env.timeout(20));
        });
        env.run();
    }

    @Test
    public void testProcess() throws Exception {
        final Env env = new Env();
        Process p1 = env.process("p1", y -> {
            y.call(env.timeout(50));
        });
        Timeout t100 = env.timeout(100, "GOT IT");

        env.process("a1", y -> {
            ConditionValue cv = (ConditionValue) y.call(p1.and("AND1", t100));
            Assert.assertEquals(100, env.getNow());
            Assert.assertEquals(2, cv.getEvents().size());
            Assert.assertEquals("GOT IT", cv.getEvents().get(1).getValue());
        });
        env.process("a2", y -> {
            ConditionValue cv = (ConditionValue) y.call(p1.or("AND2", t100));
            Assert.assertEquals(50, env.getNow());
            Assert.assertEquals(1, cv.getEvents().size());
        });
        env.run();
    }

    @Test
    public void testProcessAndInterrupt() throws Exception {
        final Env env = new Env();
        Process p1 = env.process("p1", y -> {
            try {
                y.call(env.timeout(50));
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertTrue(true);
            }
        });
        env.process("a1", y -> {
            y.call(p1.and("AND1", env.timeout(100)));
            Assert.assertEquals(100, env.getNow());
        });

        env.process("a2", y -> {
            y.call(env.timeout(10));
            p1.interrupt("failed");     // interrupt
        });
        env.run();
    }

    @Test
    public void testProcessOrInterrupt() throws Exception {
        final Env env = new Env();
        Process p1 = env.process("p1", y -> {
            try {
                y.call(env.timeout(50));
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertTrue(true);
            }
        });
        env.process("a1", y -> {
            y.call(p1.or(env.timeout(160)).env.timeout(60));
            Assert.assertEquals(60, env.getNow());
        });

        env.process("a2", y -> {
            y.call(env.timeout(10));
            p1.interrupt("failed");     // interrupt
        });
        env.run();
    }
}
