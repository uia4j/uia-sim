package uia.road;

import org.junit.Test;

import uia.road.events.JobEvent;
import uia.road.helpers.EquipStrategy;
import uia.road.helpers.ProcessTimeCalculator;

public class TimeStrategyTest implements ProcessTimeCalculator<Integer> {

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
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 10);
        j1a.setNext(j1b);

        // control move in/out
        j1b.getStrategy().getMoveIn().setFrom(15);
        j1b.getStrategy().getMoveOut().setFrom(30);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnOpEvents(true);
        factory.getLogger().printlnEquipEvents(true);
        factory.getLogger().printlnJobEvents("job1");
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
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 20);
        j1a.setNext(j1b);

        // control move in
        j1b.getStrategy().getMoveIn().setFrom(15);
        // legal before 38
        j1b.getStrategy().getMoveOut().setTo(38);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnOpEvents(true);
        factory.getLogger().printlnEquipEvents("e1");
        factory.getLogger().printlnEquipEvents("e2");
        factory.getLogger().printlnSimpleJobEvents();
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

        Factory<Integer> factory = new Factory<>(11);   // dispatch too slowly
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 20);
        j1a.setNext(j1b);

        // legal before 15
        j1b.getStrategy().getMoveIn().setTo(15);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
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
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        e1.setStrategy(new EquipStrategy<Integer>() {

            @Override
            public void update(Equip<Integer> equip, Job<Integer> job, String event) {
                if (JobEvent.MOVE_IN.equals(event)) {
                    // control move in/out time
                    job.findNextAt("o2").getStrategy().getMoveIn().setFrom(job.getMoveInTime() + 30);
                    job.findNextAt("o2").getStrategy().getMoveOut().setFrom(job.getMoveInTime() + 30 + 20);
                }
            }

        });
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 10);
        j1a.setNext(j1b);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
    }

    @Test
    public void test5() throws Exception {
        /**
         *    o1 (29) o2
         *    10      20
         *    e1------e2
         * i1:       +30
         * i2:      .  .
         * o1:         .
         * o2        +25(hold)
         */

        Factory<Integer> factory = new Factory<>(29);   // dispatch too slowly
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        e1.setStrategy(new EquipStrategy<Integer>() {

            @Override
            public void update(Equip<Integer> equip, Job<Integer> job, String event) {
                if (JobEvent.MOVE_IN.equals(event)) {
                    job.findNextAt("o2").getStrategy().getMoveIn().setFrom(job.getMoveInTime() + 30);
                    // legal before 55
                    job.findNextAt("o2").getStrategy().getMoveOut().setTo(job.getMoveInTime() + 30 + 25);
                }
            }

        });
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 20);
        j1a.setNext(j1b);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
    }

    @Test
    public void test6() throws Exception {
        /**
         *    o1 (31) o2
         *    10      20
         *    e1------e2
         * i1:         .
         * i2:       +30
         * o1:         .
         * o2          .
         */

        Factory<Integer> factory = new Factory<>(31);   // dispatch slowly
        factory.setProcessTimeCalculator(this);
        factory.setEquipStrategy(new EquipStrategy<Integer>() {

            @Override
            public void update(Equip<Integer> equip, Job<Integer> job, String event) {
                if (JobEvent.MOVE_OUT.equals(event)) {
                    job.findNextAt("o2").getStrategy().getMoveIn().setFrom(job.getMoveInTime() + 30);
                }
            }

        });

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 10);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 20);
        j1a.setNext(j1b);

        factory.prepare(j1a);
        factory.run(100);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
    }

    @Override
    public TimeInfo calc(Equip<Integer> equip, Job<Integer> job) {
        return new TimeInfo(job.getData().intValue());
    }
}
