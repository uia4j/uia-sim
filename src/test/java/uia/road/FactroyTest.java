package uia.road;

import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator;

public class FactroyTest implements ProcessTimeCalculator<Integer> {

    @Test
    public void test() throws Exception {
        Factory<Integer> factory = new Factory<>();

        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Equip<Integer> e1 = factory.createEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);

        factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());

        // p1 (o1->02)
        Job<Integer> p1o1 = new Job<>("p1", o1.getId(), 100);
        Job<Integer> p1o2 = new Job<>("p1", o2.getId(), 50);
        p1o1.setNext(p1o2);

        // p2 (o1->02)
        Job<Integer> p2o1 = new Job<>("p2", o1.getId(), 90);
        Job<Integer> p2o2 = new Job<>("p2", o2.getId(), 110);
        p2o1.setNext(p2o2);

        factory.prepare(p1o1);  // p1 at o1
        factory.prepare(p2o1);  // p2 at o1

        factory.run(500);

        factory.getReport().printlnSimpleJobEvents();
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
        factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Op<Integer> o3 = factory.createOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
        Equip<Integer> e4 = factory.createEquip("e4", 1, 2);    // block job2

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 100);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 50);
        Job<Integer> j1c = new Job<>("job1", o3.getId(), 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("job2", o1.getId(), 100);
        Job<Integer> j2b = new Job<>("job2", o2.getId(), 50);
        Job<Integer> j2c = new Job<>("job2", o3.getId(), 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.prepare(j1a);
        factory.prepare(j2a);
        factory.run(1000);
        //
        factory.getReport().printlnSimpleOpEvents();
        factory.getReport().printlnSimpleEquipEvents();
        factory.getReport().printlnSimpleJobEvents();
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
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Op<Integer> o3 = factory.createOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 2, 2);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
        Equip<Integer> e4 = factory.createEquip("e4", 1, 2);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 100);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 50);
        Job<Integer> j1c = new Job<>("job1", o3.getId(), 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("job2", o1.getId(), 100);
        Job<Integer> j2b = new Job<>("job2", o2.getId(), 250);  // cause eq4 idle
        Job<Integer> j2c = new Job<>("job2", o3.getId(), 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.prepare(j1a);
        factory.prepare(j2a);
        factory.run(1000);
        //
        factory.getReport().printlnOpEvents(true);
        factory.getReport().printlnEquipEvents(true);
        factory.getReport().printlnJobEvents(true);
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
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Op<Integer> o3 = factory.createOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
        Equip<Integer> e4 = factory.createEquip("e4", 2, 1);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 100);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 200);
        Job<Integer> j1c = new Job<>("job1", o3.getId(), 150);
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("job2", o1.getId(), 50);
        Job<Integer> j2b = new Job<>("job2", o2.getId(), 350);  // late at op3
        Job<Integer> j2c = new Job<>("job2", o3.getId(), 150);
        j2a.setNext(j2b).setNext(j2c);

        factory.prepare(j1a);               // at the operation
        factory.prepare(j2a, e1.getId());   // block job1
        factory.run(1000);
        //
        factory.getReport().printlnOpEvents(true);
        factory.getReport().printlnEquipEvents(true);
        factory.getReport().printlnJobEvents(true);
    }

    @Test
    public void test4() throws Exception {
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
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Op<Integer> o3 = factory.createOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);    // one by one
        Equip<Integer> e2 = factory.createEquip("e2", 1, 2);    // parallel
        Equip<Integer> e3 = factory.createEquip("e3", 1, 2);    // parallel
        Equip<Integer> e4 = factory.createEquip("e4", 1, 1);    // one by one

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o2.serve(e3);
        o3.serve(e4);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", "box1", o1.getId(), 100);  // waiting job2
        Job<Integer> j1b = new Job<>("job1", "box1", o2.getId(), 50);   // parallel, waiting job2
        Job<Integer> j1c = new Job<>("job1", "box1", o3.getId(), 130);  // waiting job2
        j1a.setNext(j1b).setNext(j1c);
        Job<Integer> j2a = new Job<>("job2", "box1", o1.getId(), 100);  // waiting job1
        Job<Integer> j2b = new Job<>("job2", "box1", o2.getId(), 300);  // parallel
        Job<Integer> j2c = new Job<>("job2", "box1", o3.getId(), 120);  // waiting job1
        j2a.setNext(j2b).setNext(j2c);

        // box
        factory.prepare(j1a);
        factory.prepare(j2a);
        factory.run(1000);
        //
        factory.getReport().printlnOpEvents(true);
        factory.getReport().printlnEquipEvents(true);
        factory.getReport().printlnJobEvents(true);
    }

    @Override
    public int calc(Equip<Integer> equip, Job<Integer> job) {
        return job.getData().intValue();
    }
}
