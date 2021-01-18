package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class YieldableTest {

    @Test
    public void testCallFor1() {
        Generator<Integer> gen = Yield.accept(new CallFor());
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
    public void testCallFor2() {
        Generator<Integer> gen = Yield.accept(new CallFor());
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

    public static class CallFor extends Yieldable<Integer> {

        @Override
        protected void run() {
            for (int i = 0; i < 8; i++) {
                yield(i);
            }
            yield().call(8);
            yield().call(() -> 9);
            close();
            try {
                yield(11);
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertTrue(true);
            }
        }
    }
}
