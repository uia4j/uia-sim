package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldTest {

	@Test
	public void testNextV() throws Exception {
		Generator<Integer> gen = Yield.accept(this::callFor);
		int i = 0;
		NextResult<Integer> result = null;
		while((result = gen.nextResult()).hasNext) {
			System.out.println("value=" + result.value);
			Assert.assertEquals(i, result.value.intValue());
			i++;
		}
		Assert.assertEquals(10, i);
	}


	@Test
	public void testCallFor() throws Exception {
		Generator<Integer> gen = Yield.accept(this::callFor);
		int i = 0;
		while(gen.next()) {
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
			i++;
		}
		Assert.assertEquals(10, i);
	}

	@Test
	public void testLazyFor() throws Exception {
		Generator<Integer> gen = Yield.accept(this::lazyFor);
		int i = 0;
		while(gen.next()) {
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
			i++;
		}
		Assert.assertEquals(10, i);
	}
	
	@Test
	public void testCallWhile() throws Exception {
		Generator<Integer> gen = Yield.accept(this::callWhile);
		int i = 0;
		while(gen.next()) {
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
			i++;
		}
		Assert.assertEquals(10, i);
	}

	@Test
	public void testLazyWhile() throws Exception {
		Generator<Integer> gen = Yield.accept(this::lazyWhile);
		int i = 0;
		while(gen.next()) {
			System.out.println("value=" + gen.getValue());
			Assert.assertEquals(i, gen.getValue().intValue());
			i++;
		}
		Assert.assertEquals(10, i);
	}

	public void callFor(Yield<Integer> yield) {
		for(int i = 0; i < 10; i++) {
			yield.call(i);
		}
	}

	public void lazyFor(Yield<Integer> yield) {
		for(int i = 0; i < 10; i++) {
			final int result = i;
			yield.call(() -> result);
		}
	}

	public void callWhile(Yield<Integer> yield) {
		int i = 0;
		while(true) {
			yield.call(i++);
			if(i >= 10) {
				break;
			}
		}
	}

	public void lazyWhile(Yield<Integer> yield) {
		int i = 0;
		while(true) {
			final int result = i++;
			yield.call(() -> result);
			if(i >= 10) {
				break;
			}
		}
	}
}
