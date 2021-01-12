package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldableTest {

	@Test
	public void testCallFor() {
		Generator<Integer> gen = Yield.accept(new CallFor());
		int i = 0;
		while(gen.next()) {
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
			i++;
		}
		Assert.assertEquals(10, i);
		Assert.assertTrue(gen.isClosed());
	}
	
	public static class CallFor extends Yieldable<Integer> {

		@Override
		protected void run() {
			for(int i = 0; i < 10; i++) {
				yield(i);
			}
		}
	}
}
