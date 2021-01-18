package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class Yield2WayTest {

    @Test
    public void test1() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept("y2", this::callSum2);
        int i = 0;
        gen.next();
        do {
            i = gen.getValue();
            System.out.println("value=" + i);
        }
        while (gen.next(i * i));
        Assert.assertEquals(10, i);
        Assert.assertEquals(385, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void test2() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept("y2", this::callSum2);
        int i = 0;
        gen.next();
        do {
            i = gen.getValue();
            System.out.println("value=" + i);
            if (i == 5) {
                gen.stop(i * i);
                break;
            }
        }
        while (gen.next(i * i));
        Assert.assertEquals(5, i);
        Assert.assertEquals(55, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testCallSum2FromNag() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2FromNag);
        int i = 0;
        gen.next();
        do {
            i = gen.getValue();
            System.out.println("value=" + i);
            if (i < 0) {
                gen.error(i + " < 0, not allow");	// bad design
            }
        }
        while (gen.next(i * i));
        Assert.assertEquals(10, i);
        Assert.assertEquals(385, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testCallSum2ButZero() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2ButZero);
        int i = 0;
        gen.next();
        do {
            i = gen.getValue();
            System.out.println("value=" + i);
        }
        while (gen.next(i * i));
        Assert.assertEquals(10, i);
        Assert.assertEquals(0, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testLazySum2() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::lazySum2);
        int i = 0;
        while (gen.next()) {
            i = gen.getValue();
            System.out.println("value=" + i);
            gen.send(i * i);
        }
        Assert.assertEquals(10, i);
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testError() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(this::callSum2WithError);
        int i = 0;
        gen.next();
        do {
            i = gen.getValue().intValue();
            System.out.println("value=" + gen.getValue());
            if (i > 5) {
                gen.error("i>5");
            }
        }
        while (gen.next(i * i));
        Assert.assertEquals(6, i);
        Assert.assertTrue(gen.isClosed());
    }

    public void lazySum2(Yield2Way<Integer, Integer> yield) {
        int i = 1;
        int sum = 0;
        while (i <= 10) {
            final int result = i++;
            int v = yield.call(() -> result);
            sum += v;
            System.out.println("  sum=" + sum + ", v=" + v);
        }
        Assert.assertEquals(11, i);
        Assert.assertEquals(385, sum);
    }

    public void callSum2(Yield2Way<Integer, Integer> yield) {
        Assert.assertEquals("y2", yield.toString());
        int i = 1;
        int sum = 0;
        try {
            while (i <= 10) {
                int v = yield.call(i++);
                sum += v;
                System.out.println("  sum=" + sum + ", v=" + v);
            }
            Assert.assertEquals(11, i);
            Assert.assertEquals(385, sum);
            yield.close(sum);
        }
        catch (Exception ex) {
            yield.close(sum);
        }
    }

    public void callSum2FromNag(Yield2Way<Integer, Integer> yield) {
        int i = -1;
        int sum = 0;
        try {
            yield.call(i++);
            Assert.assertTrue(false);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        while (i <= 10) {
            int v = yield.call(i++);
            sum += v;
        }
        yield.close(sum);
        Assert.assertEquals(11, i);
    }

    public void callSum2ButZero(Yield2Way<Integer, Integer> yield) {
        int i = 1;
        int sum = 0;
        while (i <= 10) {
            int v = yield.call(i++);
            sum += v;
            System.out.println("  sum=" + sum + ", v=" + v);
        }
        yield.close(0);
        Assert.assertEquals(11, i);
        Assert.assertEquals(385, sum);
    }

    public void callSum2WithError(Yield2Way<Integer, Integer> yield) {
        int i = 0;
        int sum = 0;
        try {
            while (i <= 10) {
                int v = yield.call(++i);
                sum += v;
                System.out.println("  sum=" + sum + ", v=" + v);
            }
            Assert.assertTrue(false);
        }
        catch (Exception ex) {
            Assert.assertEquals(6, i);
            Assert.assertEquals(55, sum);
            Assert.assertEquals("i>5", ex.getMessage());
        }
    }
}
