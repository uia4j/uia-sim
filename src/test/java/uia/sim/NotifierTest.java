package uia.sim;

import org.junit.Assert;
import org.junit.Test;

public class NotifierTest {

    @Test
    public void testSimple() {
        final Env env = new Env();
        Notifier<String> notifier = new Notifier<String>(env, "notifier");
        env.process("p1", y -> {
            y.call(notifier.waiting("p1"));
            Assert.assertEquals(10, env.getNow());
        });
        env.process("p2", y -> {
            y.call(notifier.waiting("p2"));
            Assert.assertEquals(10, env.getNow());
        });
        env.process("p3", y -> {
            y.call(notifier.waiting("p3", 15));
            Assert.assertEquals(10, env.getNow());

        });

        env.process("main", y -> {
            y.call(env.timeout(10));
            notifier.available("main");
        });

        env.run();
    }

    @Test
    public void testTimeout() {
        final Env env = new Env();
        Notifier<String> notifier = new Notifier<String>(env, "notifier");
        env.process("p1", y -> {
            try {
                y.call(notifier.waiting("p1"));
                Assert.assertEquals(15, env.getNow());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        env.process("p2", y -> {
            try {
                y.call(notifier.waiting("p2", 8));
                Assert.assertEquals(8, env.getNow());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        env.process("p3", y -> {
            try {
                y.call(notifier.waiting("p3", 5));
                Assert.assertEquals(5, env.getNow());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        env.process("p4", y -> {
            try {
                y.call(env.timeout(5));
                y.call(notifier.waiting("p4", 8));
                Assert.assertEquals(13, env.getNow());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        env.process("p5", y -> {
            try {
                y.call(env.timeout(5));
                y.call(notifier.waiting("p4", 20));
                Assert.assertEquals(15, env.getNow());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        env.process("main", y -> {
            y.call(env.timeout(15));
            notifier.available("main");
        });

        env.run();
    }
}
