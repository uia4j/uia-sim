package uia.road;

import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator.TimeInfo;

public class EquipBatchTest {

    @Test
    public void test1() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        EquipBatch<Integer> e1 = new EquipBatch<>("e1", factory, 4, true);
        factory.tryAddEquip(e1);

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        o1.serve(e1);

        int time = 100;
        Job<Integer> p1 = new Job<>("p1", "p1", o1.getId(), 1, null, time);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("p2", "p2", o1.getId(), 1, null, time);
        p2.setQty(7);
        Job<Integer> p3 = new Job<>("p3", "p3", o1.getId(), 1, null, time);
        p3.setQty(15);
        Job<Integer> p4 = new Job<>("p4", "p4", o1.getId(), 1, null, time);
        p4.setQty(1);

        factory.prepare(p1);
        factory.prepare(p2);
        factory.prepare(p3);
        factory.prepare(p4);

        factory.run(1000);

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Test
    public void test2() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        EquipBatch<Integer> e1 = new EquipBatch<>("e1", factory, 4, true);
        factory.tryAddEquip(e1);

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        o1.serve(e1);

        int time = 100;
        Job<Integer> p1 = new Job<>("p1", "p1", o1.getId(), 1, null, time);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("p2", "p2", o1.getId(), 1, null, time);
        p2.setQty(7);
        Job<Integer> p3 = new Job<>("p3", "p3", o1.getId(), 1, null, time);
        p3.setQty(15);
        Job<Integer> p4 = new Job<>("p4", "p4", o1.getId(), 1, null, time);
        p4.setQty(1);
        Job<Integer> p5 = new Job<>("p5", "p5", o1.getId(), 1, null, time);
        p5.setQty(5);

        factory.prepare(p1);
        factory.prepare(p2);
        factory.prepare(p3);
        factory.prepare(p4);
        factory.prepare(p5);

        factory.getEnv().process("", y -> {
            y.call(factory.getEnv().timeout(110));
            Job<Integer> p6 = new Job<>("p6", "p6", o1.getId(), 1, null, time);
            p6.setQty(5);
            factory.dispatch(p6);

            y.call(factory.getEnv().timeout(60));
            Job<Integer> p7 = new Job<>("p7", "p7", o1.getId(), 1, null, time);
            p5.setQty(7);
            factory.dispatch(p7);

        });

        factory.run(1000);

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Test
    public void test3() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        EquipBatch<Integer> e1 = new EquipBatch<>("e1", factory, 4, true);
        factory.tryAddEquip(e1);

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        o1.serve(e1);

        int time = 100;
        Job<Integer> p1 = new Job<>("p1", "p1", o1.getId(), 1, null, time);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("p2", "p2", o1.getId(), 1, null, time);
        p2.setQty(7);
        Job<Integer> p3 = new Job<>("p3", "p3", o1.getId(), 1, null, time);
        p3.setQty(15);
        Job<Integer> p4 = new Job<>("p4", "p4", o1.getId(), 1, null, time);
        p4.setQty(1);
        Job<Integer> p5 = new Job<>("p5", "p5", o1.getId(), 1, null, time);
        p4.setQty(1);

        e1.addReserved(p5);

        factory.prepare(p1);
        factory.prepare(p2);
        factory.prepare(p3);
        factory.prepare(p4);

        factory.getEnv().process("", y -> {
            y.call(factory.getEnv().timeout(10));
            factory.dispatch(p5);
        });

        factory.run(1000);

        factory.getLogger().printlnSimpleEquipEvents();
    }
}
