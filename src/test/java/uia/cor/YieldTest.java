package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldTest {

    @Test
    public void testCallFor1() {
        Generator<Integer> gen = Yield.accept("y1", this::callFor);
        int i = 0;
        while (gen.next()) {
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testCallFor2() {
        Generator<Integer> gen = Yield.accept("y1", this::callFor);
        int i = 0;
        NextResult<Integer> nr;
        while ((nr = gen.nextResult()).hasNext) {
            Assert.assertEquals(i, nr.value.intValue());
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
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testError1() {
        Generator<Integer> gen = Yield.accept(this::callForWithError1);
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

    @Test
    public void testError2() {
        Generator<Integer> gen = Yield.accept(this::callForWithError2);
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
            if (i == 5) {
                Assert.assertTrue(gen.errorNext(new Exception("i=5")));
                Assert.assertEquals(5, gen.getValue().intValue());
                i++;
            }
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testStop1() {
        Generator<Integer> gen = Yield.accept("y1", this::callFor);
        int i = 0;
        while (gen.next()) {
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
            if (i == 5) {
                gen.stop();
            }
        }
        Assert.assertEquals(5, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testStop2() {
        Generator<Integer> gen = Yield.accept("y1", this::callFor);
        int i = 0;
        while (gen.next()) {
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
            if (i == 5) {
                gen.stop(new Exception("force stop"));
            }
        }
        Assert.assertEquals(5, i);
        Assert.assertTrue(gen.isClosed());
    }

    public void callFor(Yield<Integer> yield) {
        Assert.assertEquals("y1", yield.toString());
        for (int i = 0; i < 10; i++) {
            try {
                yield.call(i);
            }
            catch (Exception ex) {
                Assert.assertEquals("force stop", ex.getMessage());
                return;
            }
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
        while (yield.isAlive()) {
            if (i == 10) {
                yield.callLast(i);
            }
            else {
                yield.call(i);
            }
            i++;
        }
        yield.close();
    }

    public void lazyWhile(Yield<Integer> yield) {
        int i = 0;
        while (yield.isAlive()) {
            final int result = i++;
            if (result == 10) {
                yield.callLast(() -> result);
            }
            else {
                yield.call(() -> result);
            }
        }
        yield.close();
    }

    public void callForWithError1(Yield<Integer> yield) {
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

    public void callForWithError2(Yield<Integer> yield) {
        for (int i = 0; i < 10; i++) {
            try {
                yield.call(i);
            }
            catch (Exception e) {
                Assert.assertEquals("i=5", e.getMessage());
            }
        }
    }
}
