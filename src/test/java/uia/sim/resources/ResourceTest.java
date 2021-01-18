package uia.sim.resources;

import org.junit.Assert;
import org.junit.Test;

import uia.sim.Env;
import uia.sim.Processable;
import uia.sim.resources.Resource.Request;

public class ResourceTest {

    @Test
    public void test() throws Exception {
        final Env env = new Env();
        final Resource res = new Resource(env, 2);

        env.process(new Car("car1", res, 100));
        env.process(new Car("car2", res, 110));
        env.process(new Car("car3", res, 90));
        env.process(new Car("car4", res, 120));

        env.run();
    }

    public static class Car extends Processable {

        private final Resource res;

        private final int driving;

        protected Car(String id, Resource res, int driving) {
            super(id);
            this.res = res;
            this.driving = driving;
        }

        @Override
        protected void run() {
            try {
                yield(env().timeout(this.driving));
                System.out.println(getId() + " stop   at " + now());
                try (Request req = this.res.request(getId() + "_charge")) {
                    yield(req);
                    System.out.println(getId() + " charge at " + now());
                    yield(env().timeout(40));
                    System.out.println(getId() + " leave  at " + now());
                }
            }
            catch (Exception ex) {
                Assert.assertTrue(false);
            }
        }

    }
}
