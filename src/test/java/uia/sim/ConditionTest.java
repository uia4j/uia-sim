package uia.sim;

import org.junit.Assert;
import org.junit.Test;

public class ConditionTest {

    @Test
    public void testAnd() throws Exception {
        final Env env = new Env();
        env.process("testAnd", y -> {
            y.call(env.timeout(10).and("and2", env.timeout(20)).and("and3", env.timeout(30)));
            Assert.assertEquals(30, env.getNow());
        });
        env.run();
    }

    @Test
    public void testAndOr() throws Exception {
        final Env env = new Env();
        env.setListener(new EnvListenerAdapter());
        env.process("testAndOr", y -> {
            Event event1 = env.timeout(10).and("and1", env.timeout(20)).and("and1", env.timeout(30));
            Event event2 = env.timeout(15).and("and2", env.timeout(25));
            y.call(event1.or("andOr", event2));
            Assert.assertEquals(25, env.getNow());
        });
        env.run();
    }

    @Test
    public void testOr() throws Exception {
        final Env env = new Env();
        env.process("testOr", y -> {
            y.call(env.timeout(30).or("or2", env.timeout(20)).or("or3", env.timeout(10)));
            Assert.assertEquals(10, env.getNow());
        });
        env.run();
    }

    @Test
    public void testOrAnd() throws Exception {
        final Env env = new Env();
        env.setListener(new EnvListenerAdapter());
        env.process("testOrAnd", y -> {
            Event event1 = env.timeout(10).or("and1", env.timeout(30).or("and1", env.timeout(40)));
            Event event2 = env.timeout(15).or("and2", env.timeout(20));
            y.call(event1.and("andOr", event2));
            Assert.assertEquals(15, env.getNow());
        });
        env.run();
    }
}
