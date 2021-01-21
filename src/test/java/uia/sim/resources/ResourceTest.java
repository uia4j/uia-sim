package uia.sim.resources;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.Env;
import uia.sim.Processable;
import uia.sim.events.Process;
import uia.sim.resources.Resource.Request;

public class ResourceTest {

    @Test
    public void test1() throws Exception {
        final Env env = new Env();
        final Resource res = new Resource(env, 2);

        env.process(new Car1("car1", res, 100));
        env.process(new Car1("car2", res, 110));
        env.process(new Car1("car3", res, 90));
        env.process(new Car1("car4", res, 120));

        env.run();
    }

    @Test
    public void test2() throws Exception {
        final Env env = new Env();
        final Resource res = new Resource(env, 2);

        env.process(new Car1("car1", res, 100));
        env.process(new Car1("car2", res, 110));
        env.process(new Car1("car3", res, 120));
        Process car3 = env.process(new Car2("car4", res, 120));
        env.process("car4_leave", y -> {
            y.call(env.timeout(130));
            car3.interrupt("can not wait");
        });

        env.run();
    }

    public static class Car1 extends Processable {

        private final Resource res;

        private final int driving;

        public Car1(String id, Resource res, int driving) {
            super(id);
            this.res = res;
            this.driving = driving;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.driving));
            System.out.println(getId() + " stop   at " + now());
            try (Request req = this.res.request(getId() + "_charge")) {
                yield(req);
                System.out.println(getId() + " refuel at " + now());
                yield(env().timeout(40));
                System.out.println(getId() + " done   at " + now());
            }
            catch (Exception ex) {
                Assert.assertTrue(false);
            }
        }
    }

    public static class Car2 extends Processable {

        private final Resource res;

        private final int driving;

        public Car2(String id, Resource res, int driving) {
            super(id);
            this.res = res;
            this.driving = driving;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.driving));
            System.out.println(getId() + " stop   at " + now());
            try (Request req = this.res.request(getId() + "_charge")) {
                yield(req);
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                System.out.println(getId() + " leave  at " + now());
                Assert.assertEquals("can not wait", ex.getMessage());
                Assert.assertTrue(true);
            }
        }
    }
}
