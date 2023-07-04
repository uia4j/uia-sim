package uia.road;

import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator;

public class EquipMuchTest implements ProcessTimeCalculator<Integer> {

    @Test
    public void test1() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 4);
        o1.serve(e1);

        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        Job<Integer> p1 = new Job<>("1", "p1", o1.getId(), 1, null, 100);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("1", "p2", o1.getId(), 1, null, 100);
        p2.setQty(7);

        factory.prepare(p1);
        factory.prepare(p2);

        factory.run(2000);

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Test
    public void test2() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 2, 2);
        o1.serve(e1);

        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        Job<Integer> p1 = new Job<>("p1", "p1", o1.getId(), 1, null, 100);
        p1.setQty(1);
        Job<Integer> p2 = new Job<>("p2", "p2", o1.getId(), 1, null, 100);
        p2.setQty(1);
        Job<Integer> p3 = new Job<>("p3", "p3", o1.getId(), 1, null, 100);
        p2.setQty(1);

        factory.prepare(p1);
        factory.prepare(p2);
        factory.prepare(p3);

        e1.addReserved(p3);

        factory.run(2000);

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Test
    public void test3() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        EquipMuch<Integer> e1 = (EquipMuch<Integer>) factory.tryCreateEquip("e1", 2, 2);
        e1.setCompensationTime(6);
        e1.getChannels().forEach(ch -> {
            ch.setBatchSize(5);
            ch.setCompensationTime(5);
        });
        o1.serve(e1);

        factory.setProcessTimeCalculator((e, j) -> new TimeInfo(j.getData().intValue()));

        Job<Integer> p1 = new Job<>("p1", "p1", o1.getId(), 1, null, 100);
        p1.setQty(5);

        factory.prepare(p1);

        factory.run(2000);

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Override
    public TimeInfo calc(Equip<Integer> equip, Job<Integer> job) {
        return new TimeInfo(job.getData().intValue());
    }
}
