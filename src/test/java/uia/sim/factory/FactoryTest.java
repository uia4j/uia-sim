package uia.sim.factory;

import org.junit.Test;

import uia.sim.Env;
import uia.sim.Event.PriorityType;

public class FactoryTest {

    @Test
    public void test1() {
        Env env = new Env();

        Operation op1 = new Operation("op1");
        Operation op2 = new Operation("op2");

        Equipment eq1 = new Equipment("eq1");
        Equipment eq2 = new Equipment("eq2");
        Equipment eq3 = new Equipment("eq3");

        eq1.bind(op1);
        eq1.bind(op2);
        eq2.bind(op1);
        eq3.bind(op1);

        op1.enqueue("p11");
        op1.enqueue("p12");
        op1.enqueue("p13");
        op1.enqueue("p14");
        op1.enqueue("p15");
        op2.enqueue("p21");
        op2.enqueue("p22");

        env.process(eq1);
        env.process(eq2);
        env.process(eq3);

        env.run(3100);
    }

    @Test
    public void test2() {
        Env env = new Env();

        Operation op1 = new Operation("op1");
        // Operation op2 = new Operation("op2");
        // Operation op3 = new Operation("op3");

        Equip eq1 = new Equip("eq1");
        Equip eq2 = new Equip("eq2");

        eq1.bind(op1);
        eq2.bind(op1);

        env.process(eq1);
        env.process(eq2);

        env.schedule("dispatch", 10, PriorityType.NORMAL, () -> op1.enqueue("p11"));
        env.schedule("dispatch", 10, PriorityType.NORMAL, () -> op1.enqueue("p12"));
        env.schedule("dispatch", 140, PriorityType.NORMAL, () -> op1.enqueue("p13"));
        env.schedule("dispatch", 150, PriorityType.NORMAL, () -> op1.enqueue("p14"));

        env.run(550);
    }
}
