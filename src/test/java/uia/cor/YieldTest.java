package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldTest {

    @Test
    public void testCallFor() {
        Generator<Integer> gen = Yield.accept(this::callFor);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testLazyFor() {
        Generator<Integer> gen = Yield.accept(this::lazyFor);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testCallWhile() {
        Generator<Integer> gen = Yield.accept(this::callWhile);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testLazyWhile() throws Exception {
        Generator<Integer> gen = Yield.accept(this::lazyWhile);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testError() {
        Generator<Integer> gen = Yield.accept(this::callForWithError);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
            if (i > 5) {
                gen.error("i>5");
            }
        }
        Assert.assertEquals(6, i);
        Assert.assertTrue(gen.isClosed());
    }

    public void callFor(Yield<Integer> yield) {
        for (int i = 0; i < 10; i++) {
            yield.call(i);
        }
    }

    public void lazyFor(Yield<Integer> yield) {
        for (int i = 0; i < 10; i++) {
            final int result = i;
            yield.call(() -> result);
        }
    }

    public void callWhile(Yield<Integer> yield) {
        int i = 0;
        while (true) {
            yield.call(i++);
            if (i >= 10) {
                break;
            }
        }
    }

    public void lazyWhile(Yield<Integer> yield) {
        int i = 0;
        while (true) {
            final int result = i++;
            yield.call(() -> result);
            if (i >= 10) {
                break;
            }
        }
    }

    public void callForWithError(Yield<Integer> yield) {
        try {
            for (int i = 0; i < 10; i++) {
                yield.call(i);
            }
            Assert.assertTrue(false);
        }
        catch (Exception e) {
            Assert.assertEquals("i>5", e.getMessage());
        }
    }
}
