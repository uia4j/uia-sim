package uia.road;

import java.util.List;

import org.junit.Test;

import uia.road.events.JobEvent;
import uia.road.helpers.EquipStrategy;
import uia.road.helpers.JobSelector;
import uia.road.helpers.ProcessTimeCalculator;

public class TimeStrategyTest implements ProcessTimeCalculator<Integer>, JobSelector<Integer> {

    @Test
    public void test1() throws Exception {
        /**
         *    o1   o2
         *    10   10
         *    e1---e2
         * i1:     15(cause MO delay, pass)
         * i2:      .
         * o1:     30(cause MO delays, pass)
         * o2:      .
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        e2.setJobSelector(this);
        e2.setWaitingMaxTime(12);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 10);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 10);
        j1a.setNext(j1b);
        j1b.setPrev(j1a);

        j1b.getStrategy().getMoveIn().setFrom(15);
        j1b.getStrategy().getMoveOut().setFrom(30);

        factory.preload(j1a);
        factory.run(100);
        //
        factory.getReport().printlnOp(true);
        factory.getReport().printlnEquip(true);
        factory.getReport().printlnJob(true);
    }

    @Test
    public void test2() throws Exception {
        /**
         *    o1 (11) o2
         *    10      20
         *    e1------e2
         * i1:        15
         * i2:         .
         * o1:         .
         * o2:        35(cause MO illegal, hold)
         */

        Factory<Integer> factory = new Factory<>(11);   // path time: 11
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        e2.setJobSelector(this);
        e2.setWaitingMaxTime(12);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 10);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 20);
        j1a.setNext(j1b);
        j1b.setPrev(j1a);

        j1b.getStrategy().getMoveIn().setFrom(15);
        j1b.getStrategy().getMoveOut().setTo(38);

        factory.preload(j1a);
        factory.run(100);
        //
        factory.getReport().printlnOp(true);
        factory.getReport().printlnEquip(true);
        factory.getReport().printlnJob(true);
    }

    @Test
    public void test3() throws Exception {
        /**
         *    o1 (11) o2
         *    10      20
         *    e1------e2
         * i1:         .
         * i2:        15(cause enqueue illegal, hold)
         * o1:         .
         * o2:         .
         */

        Factory<Integer> factory = new Factory<>(11);   // path time: 11
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        e2.setJobSelector(this);
        e2.setWaitingMaxTime(12);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 10);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 20);
        j1a.setNext(j1b);
        j1b.setPrev(j1a);

        j1b.getStrategy().getMoveIn().setTo(15);

        factory.preload(j1a);
        factory.run(100);
        //
        factory.getReport().printlnOp(true);
        factory.getReport().printlnEquip(true);
        factory.getReport().printlnJob(true);
    }

    @Test
    public void test4() throws Exception {
        /**
         *    o1   o2
         *    10   10
         *    e1---e2
         * i1:    +30 
         * i2:      .
         * o1:    +20(pass)
         * o2       .
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        e1.setStrategy(new EquipStrategy<Integer>() {

            @Override
            public void update(Equip<Integer> equip, Job<Integer> job, String event) {
                int now = equip.getFactory().now();
                if (JobEvent.MOVE_IN.equals(event)) {
                    job.findNextAt("o2").getStrategy().getMoveIn().setFrom(now + 30);
                    job.findNextAt("o2").getStrategy().getMoveOut().setFrom(now + 30 + 20);
                }
            }

        });
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        e2.setJobSelector(this);
        e2.setWaitingMaxTime(12);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 10);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 10);
        j1a.setNext(j1b);
        j1b.setPrev(j1a);

        factory.preload(j1a);
        factory.run(100);
        //
        factory.getReport().printlnOp(true);
        factory.getReport().printlnEquip(true);
        factory.getReport().printlnJob(true);
    }

    @Test
    public void test5() throws Exception {
        /**
         *    o1 (30) o2
         *    10      20
         *    e1------e2
         * i1:       +30
         * i2:      .  .
         * o1:         .
         * o2        +20(hold)
         */

        Factory<Integer> factory = new Factory<>(30);   // dispatch too slowly
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        e1.setStrategy(new EquipStrategy<Integer>() {

            @Override
            public void update(Equip<Integer> equip, Job<Integer> job, String event) {
                int now = equip.getFactory().now();
                if (JobEvent.MOVE_IN.equals(event)) {
                    job.findNextAt("o2").getStrategy().getMoveIn().setFrom(now + 30);
                    job.findNextAt("o2").getStrategy().getMoveOut().setTo(now + 30 + 25);
                }
            }

        });
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        e2.setJobSelector(this);
        e2.setWaitingMaxTime(12);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 10);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 20);
        j1a.setNext(j1b);
        j1b.setPrev(j1a);

        factory.preload(j1a);
        factory.run(100);
        //
        factory.getReport().printlnOp(true);
        factory.getReport().printlnEquip(true);
        factory.getReport().printlnJob(true);
    }

    @Override
    public int calc(Equip<Integer> equip, Job<Integer> job) {
        return job.getData().intValue();
    }

    @Override
    public SelectResult<Integer> select(Equip<Integer> equip, List<Job<Integer>> jobs) {
        if (jobs.isEmpty()) {
            return new SelectResult<>();
        }
        Job<Integer> job = jobs.get(0);
        return new SelectResult<>(job);
    }
}
