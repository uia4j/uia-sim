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
		}
		while(gen.next(i * i));
		Assert.assertEquals(10, i);
		Assert.assertTrue(gen.isClosed());
	}
	
	@Test
	public void testLazySum2() {
		Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2);
		int i = 0;
		gen.next();
		do {
			i = gen.getValue().intValue();
			System.out.println("value=" + gen.getValue());
		}
		while(gen.next(i * i));
		Assert.assertEquals(10, i);
		Assert.assertTrue(gen.isClosed());
	}

	@Test
	public void testTerminate() {
		Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2WithInterrput);
		int i = 0;
		gen.next();
		do {
			i = gen.getValue().intValue();
			System.out.println("value=" + gen.getValue());
			if(i > 5) {
				gen.interrupt();
				break;
			}
		}
		while(gen.next(i * i));
		Assert.assertEquals(6, i);
		Assert.assertTrue(gen.isClosed());
	}

	public void callSum2(Yield2Way<Integer, Integer> yield) {
		try {
			int i = 1;
			int sum = 0;
			System.out.println("  sum=" + sum);
			while(i <= 10) {
				int v = yield.call(i++);
				sum += v;
				System.out.println("  sum=" + sum + ", v=" + v);
			}
			Assert.assertEquals(11, i);
			Assert.assertEquals(385, sum);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	public void lazySum2(Yield2Way<Integer, Integer> yield) {
		try {
			int i = 1;
			int sum = 0;
			System.out.println("  sum=" + sum);
			while(i <= 10) {
				final int result = i;
				int v = yield.call(() -> result);
				sum += v;
				System.out.println("  sum=" + sum + ", v=" + v);
			}
			Assert.assertEquals(11, i);
			Assert.assertEquals(385, sum);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
	}

	public void callSum2WithInterrput(Yield2Way<Integer, Integer> yield) {
		int i = 0;
		int sum = 0;
		try {
			System.out.println("  sum=" + sum);
			while(i <= 10) {
				int v = yield.call(++i);
				sum += v;
				System.out.println("  sum=" + sum + ", v=" + v);
			}
			Assert.assertTrue(false);
		}
		catch(InterruptedException ex) {
			Assert.assertEquals(6, i);
			Assert.assertEquals(55, sum);
			System.out.println(ex.getMessage());
		}
	}
}
