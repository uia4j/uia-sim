package uia.road;

import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator.TimeInfo;

public class ProcessTimeCalculatorTest {

    @Test
    public void test1() throws Exception {
        /**
         * o1 (10) o2 (20) o3
         *
         * e1------e2------e3
         *
         */

        Factory<Integer> factory = new Factory<>();
        // global process time calculator
        factory.setProcessTimeCalculator((e, j) -> TimeInfo.create(j.getData().intValue()));
        factory.setPathTimeCalculator((f, t) -> {
            if ("o2".equals(t.getId())) {
                return 10;
            }
            return 20;
        });

        // build operations
        Op<Integer> o1 = factory.tryCreateOperation("o1");
        Op<Integer> o2 = factory.tryCreateOperation("o2");
        Op<Integer> o3 = factory.tryCreateOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.tryCreateEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.tryCreateEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.tryCreateEquip("e3", 1, 1);

        // custom process time calculator
        e2.setProcessTimeCalculator((e, j) -> TimeInfo.create(j.getData().intValue() * 2));

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o3.serve(e3);

        // build jobs
        Job<Integer> j1a = new Job<>("1", "job1", o1.getId(), 1, null, 100);
        Job<Integer> j1b = new Job<>("2", "job1", o2.getId(), 1, null, 100);
        Job<Integer> j1c = new Job<>("3", "job1", o3.getId(), 1, null, 100);
        j1a.setNext(j1b).setNext(j1c);

        factory.prepare(j1a);
        factory.run(500);
        //
        System.out.println("operations:");
        factory.getLogger().printlnOpEvents(false);
        System.out.println("equipment:");
        factory.getLogger().printlnEquipEvents(false);
        System.out.println("jobs:");
        factory.getLogger().printlnJobEvents(false);
    }
}
