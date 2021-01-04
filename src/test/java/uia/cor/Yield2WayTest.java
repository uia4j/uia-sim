package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class Yield2WayTest {

	
	@Test
	public void testCallSum2() {
		Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2);
		int i = 0;
		gen.next();
		do {
			i = gen.getValue().intValue();
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
		}
		while(gen.next(i * i));
		Assert.assertEquals(10, i);
		Assert.assertTrue(gen.isClosed());
	}

	public void callSum2(Yield2Way<Integer, Integer> yield) {
		try {
			int i = 1;
			int sum = 0;
			while(true) {
				int v = yield.call(i++);
				sum += v;
				System.out.println("  sum=" + sum + ", v=" + v);
				if(i > 10) {
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void callForButTerminated(Yield<Integer> yield) {
		try {
			for(int i = 0; i < 10; i++) {
				yield.call(i);
			}
			Assert.assertTrue(false);
		}
		catch(InterruptedException ex) {
			System.out.println(ex.getMessage());
		}
	}
}
