package uia.sim.resources;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.Env;
import uia.sim.Processable;
import uia.sim.resources.Container.Request;

public class ContainerTest {

    @Test
    public void test() throws Exception {
        final Env env = new Env();
        final Container res = new Container(env, 100);

        env.process(new Car1("car1", res, 50, 50));
        env.process(new Car1("car2", res, 50, 10));
        env.process(new Car1("car3", res, 40, 40));
        env.process(new Car1("car4", res, 100, 90));
        env.process(new Car1("car4", res, 20, 20));

        env.process("fill", y -> {
            y.call(env.timeout(100));
            res.release("f1", 100);
            y.call(env.timeout(100));
            res.release("f2", 100);
            y.call(env.timeout(100));
        });

        env.run();
    }

    public static class Car1 extends Processable {

        private final Container res;

        private final int planUsage;

        private final int actualUsage;

        public Car1(String id, Container res, int planUsage, int actualUsage) {
            super(id);
            this.res = res;
            this.planUsage = planUsage;
            this.actualUsage = actualUsage;
        }

        @Override
        protected void run() {
            try (Request req = this.res.request(getId() + "_charge", this.planUsage)) {
                yield(req);
                System.out.println(getId() + " done at " + now());
                req.consume(this.actualUsage);
            }
            catch (Exception ex) {
                Assert.assertTrue(false);
            }
        }

        @Override
        protected void initial() {
        }
    }
}
