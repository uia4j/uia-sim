package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldableTest {

    @Test
    public void test1() {
        Generator<Integer> gen = Yield.accept(new CallFor1());
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
    public void test2() {
        Generator<Integer> gen = Yield.accept(new CallFor2());
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
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
    public void test3() {
        Generator<Integer> gen = Yield.accept(new CallFor2());
        int i = 0;
        while (gen.next()) {
            System.out.println("value=" + gen.getValue());
            Assert.assertEquals(i, gen.getValue().intValue());
            i++;
            if (i == 6) {
                gen.stop(new Exception("force stop"));
            }
        }
        Assert.assertEquals(6, i);
        Assert.assertTrue(gen.isClosed());
    }

    public static class CallFor1 extends Yieldable<Integer> {

        @Override
        protected void run() {
            for (int i = 0; i < 8; i++) {
                yield(i);
            }
            yield().call(8);
            yield().call(() -> 9);
        }
    }

    public static class CallFor2 extends Yieldable<Integer> {

        @Override
        protected void run() {
            int i = 0;
            try {
                for (i = 0; i < 8; i++) {
                    yield(i);
                }
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertEquals(i, 5);
                Assert.assertTrue(true);
            }
            close();
        }
    }
}
