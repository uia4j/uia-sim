package uia.road;

import org.junit.Test;

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
        factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());
        factory.setPathTimeCalculator((f, t) -> {
            if ("o2".equals(t.getId())) {
                return 10;
            }
            return 20;
        });

        // build operations
        Op<Integer> o1 = factory.createOperation("o1");
        Op<Integer> o2 = factory.createOperation("o2");
        Op<Integer> o3 = factory.createOperation("o3");

        // build equipments
        Equip<Integer> e1 = factory.createEquip("e1", 1, 1);
        Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
        Equip<Integer> e3 = factory.createEquip("e3", 1, 1);

        // custom process time calculator
        e2.setProcessTimeCalculator((e, j) -> j.getData().intValue() * 2);

        // bind operations and equipments
        o1.serve(e1);
        o2.serve(e2);
        o3.serve(e3);

        // build jobs
        Job<Integer> j1a = new Job<>("job1", o1.getId(), 100);
        Job<Integer> j1b = new Job<>("job1", o2.getId(), 100);
        Job<Integer> j1c = new Job<>("job1", o3.getId(), 100);
        j1a.setNext(j1b).setNext(j1c);

        factory.prepare(j1a);
        factory.run(500);
        //
        System.out.println("operations:");
        factory.getReport().printlnOpEvents(false);
        System.out.println("equipment:");
        factory.getReport().printlnEquipEvents(false);
        System.out.println("jobs:");
        factory.getReport().printlnJobEvents(false);
    }
}
