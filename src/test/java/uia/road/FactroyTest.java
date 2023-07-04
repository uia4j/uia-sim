package uia.road;

import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator;

public class FactroyTest implements ProcessTimeCalculator<Integer> {

    @Test
    public void test() throws Exception {
        /**
         * o1 o2
         *    e2
         *   /
         * e1
         *   \
         *    e3
         */
        Factory<Integer> factory = new Factory<>();

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 2, 2);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 1);
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);

        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        // p1 (o1->02)
        Job<Integer> p1o1 = new Job<>("1", "p1", o1.getId(), 1, null, 100);
        p1o1.setQty(3);
        Job<Integer> p1o2 = new Job<>("2", "p1", o2.getId(), 1, null, 50);
        p1o2.setQty(3);
        p1o1.setNext(p1o2);

        // p2 (o1->02)
        Job<Integer> p2o1 = new Job<>("1", "p2", o1.getId(), 1, null, 90);
        Job<Integer> p2o2 = new Job<>("2", "p2", o2.getId(), 1, null, 110);
        p2o1.setNext(p2o2);

        factory.prepare(p1o1);  // p1 at o1
        factory.prepare(p2o1);  // p2 at o1

        factory.run(500);

        factory.getLogger().printlnSimpleJobEvents();
    }

    @Test
    public void test1() throws Exception {
        /**
         * o1 o2 o3
         *    e2
         *   /  \
         * e1    e4
         *   \  /
         *    e3
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");
        Op<Integer> o3 = factory.tryCreateOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 2, 2);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 2, 2);
        Equip<Integer> e4 = factory.tryCreateEquip("e4", 1, 2);    // move in job2 at 300

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 100);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 50);
        Job<Integer> j1c = new Job<>("3", "job1", o3.getId(), 1, null, 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("1", "job2", o1.getId(), 1, null, 100);
        Job<Integer> j2b = new Job<>("2", "job2", o2.getId(), 1, null, 60);
        Job<Integer> j2c = new Job<>("3", "job2", o3.getId(), 1, null, 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.prepare(j1a);
        factory.prepare(j2a);
        factory.run(1000);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
    }

    @Test
    public void test2() throws Exception {
        /**
         * o1 o2 o3
         *    e2
         *   /  \
         * e1    e4
         *   \  /
         *    e3
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");
        Op<Integer> o3 = factory.tryCreateOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 1);
        Equip<Integer> e4 = factory.tryCreateEquip("e4", 1, 2);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 100);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 50);
        Job<Integer> j1c = new Job<>("3", "job1", o3.getId(), 1, null, 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("1", "job2", o1.getId(), 1, null, 100);
        Job<Integer> j2b = new Job<>("2", "job2", o2.getId(), 1, null, 250);  // cause eq4 idle 50s
        Job<Integer> j2c = new Job<>("3", "job2", o3.getId(), 1, null, 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.prepare(j1a);
        factory.prepare(j2a);
        factory.run(1000);
        //
        factory.getLogger().printlnOpEvents(true);
        factory.getLogger().printlnEquipEvents(true);
        factory.getLogger().printlnJobEvents(true);
    }

    @Test
    public void test3() throws Exception {
        /**
         * o1 o2 o3
         *    e2
         *   /  \
         * e1    e4
         *   \  /
         *    e3
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator(this);

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");
        Op<Integer> o3 = factory.tryCreateOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 1);
        Equip<Integer> e4 = factory.tryCreateEquip("e4", 2, 2);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 100);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 200);
        Job<Integer> j1c = new Job<>("3", "job1", o3.getId(), 1, null, 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("1", "job2", o1.getId(), 1, null, 50);
        Job<Integer> j2b = new Job<>("2", "job2", o2.getId(), 1, null, 350);  // late at op3
        Job<Integer> j2c = new Job<>("3", "job2", o3.getId(), 1, null, 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.dispatch(j2a, e1.getId());          // block job1, release job1 at 50
        factory.prepare(j1a);                       // at the operation
        factory.run(1000);
        //
        factory.getLogger().printlnOpEvents(true);
        factory.getLogger().printlnEquipEvents(true);
        factory.getLogger().printlnJobEvents(true);
    }

    @Test
    public void test4() throws Exception {
        /**
         * o1 o2 o3
         *
         * e1
         * e2
         * e3
         *
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 2);

        // bind operations and equipments
        o1.serve(e1);
        o1.serve(e2);
        o1.serve(e3);

        // build jobs
        for (int i = 1; i < 10; i++) {
            factory.prepare(new Job<>("p" + i, "p" + i, o1.getId(), 1, null, 100));
        }
        factory.run(1000);
        //
        factory.getLogger().printlnSimpleOpEvents();
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
    }

    @Test
    public void test5() throws Exception {
        /**
         *      e1
         *    /
         * o1
         *    \
         *      e2, e3
         *    /
         * o2
         *    \
         *      e4
         *
         */

        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 2);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 2);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 2);
        Equip<Integer> e4 = factory.tryCreateEquip("e3", 1, 2);

        // bind operations and equipments
        o1.serve(e1);
        o1.serve(e2);
        o1.serve(e3);
        o2.serve(e2);
        o2.serve(e3);
        o2.serve(e4);

        // build jobs
        for (int i = 1; i < 10; i++) {
            factory.prepare(new Job<>("a" + i, "a" + i, o1.getId(), 1, null, 50));
        }
        for (int i = 1; i < 10; i++) {
            factory.prepare(new Job<>("b" + i, "b" + i, o2.getId(), 1, null, 50));
        }
        factory.run(1000);
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
