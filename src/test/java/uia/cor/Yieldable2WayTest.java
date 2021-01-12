package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class Yieldable2WayTest {
	
	@Test
	public void testCallSum2() {
		Generator2Way<Integer, Integer> gen = Yield2Way.accept(new CallSum2());
		int i = 0;
		gen.next();
		do {
			i = gen.getValue();
			System.out.println("value=" + i);
		}
		while(gen.next(i * i));
		Assert.assertEquals(10, i);
		Assert.assertEquals(385, (int)gen.getResult());
		Assert.assertTrue(gen.isClosed());
	}

	public static class CallSum2 extends Yieldable2Way<Integer, Integer> {
		@Override
		protected void run() {
			int i = 1;
			int sum = 0;
			while(i <= 10) {
				int v = yield(i++);
				sum += v;
				System.out.println("  sum=" + sum + ", v=" + v);
			}
			
			close(sum);
			Assert.assertEquals(11, i);
			Assert.assertEquals(385, sum);
		}
	}
}
