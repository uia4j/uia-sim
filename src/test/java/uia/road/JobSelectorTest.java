package uia.road;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import uia.road.JobSelectorTest.Domain;
import uia.road.helpers.JobSelector;

public class JobSelectorTest implements JobSelector<Domain> {

    @Test
    public void test1() throws Exception {
        /**
         * o1
         *
         * e1
         * 
         */

        Factory<Domain> factory = new Factory<>(10);
        // global process time calculator
        factory.setProcessTimeCalculator((e, j) -> j.getData().pt);

        // build operations
        Op<Domain> o1 = factory.createOperation("o1");

        // build equipments
        Equip<Domain> e1 = factory.createEquip("e1", 1, 1);
        e1.setJobSelector(this);

        // bind operations and equipments
        o1.serve(e1);

        // build jobs
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            factory.prepare(new Job<>("job" + i, o1.getId(), new Domain(r.nextInt(5), 20)));
        }

        factory.run(1000);
        //
        System.out.println("jobs:");
        factory.getReport().printlnJobEvents(false);
    }

    public static class Domain {

        public final int priority;

        public final int pt;

        public Domain(int priority, int pt) {
            this.priority = priority;
            this.pt = pt;
        }
    }

    @Override
    public SelectResult<Domain> select(Equip<Domain> equip, List<Job<Domain>> jobs) {
        if (jobs.isEmpty()) {
            return new SelectResult<>();
        }

        Collections.sort(jobs, (a, b) -> a.getData().priority - b.getData().priority);
        return new SelectResult<Domain>(jobs.get(0));
    }
}
