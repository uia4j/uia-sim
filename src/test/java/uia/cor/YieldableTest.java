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

    public static class CallFor1 extends Yieldable<Integer> {

        @Override
        protected void run() {
            for (int i = 0; i < 8; i++) {
                yield(i);
            }
            yield().call(8);
            yield().call(() -> 9);
            close();
        }
    }
}
