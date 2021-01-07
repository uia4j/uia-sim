package uia.sim.factory;

import org.junit.Test;

import uia.sim.Env;

public class FactoryTest {

	@Test
	public void test() {
		Env env = new Env();

		Operation op1 = new Operation();
		Operation op2 = new Operation();
		
		Equipment eq1 = new Equipment(env, "eq1");
		Equipment eq2 = new Equipment(env, "eq2");
		Equipment eq3 = new Equipment(env, "eq3");

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
		
		env.run(5000);
	}
}
