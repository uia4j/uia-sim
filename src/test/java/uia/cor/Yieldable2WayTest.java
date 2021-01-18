package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class Yieldable2WayTest {

    @Test
    public void testCallSum2() {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(new CallSum2());
        int i = 0;
        NextResult<Integer> nr;
        while ((nr = gen.nextResult(i * i)).hasNext) {
            i = nr.value.intValue();
            System.out.println("value=" + i);
        }
        Assert.assertEquals(10, i);
        Assert.assertEquals(385, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    public static class CallSum2 extends Yieldable2Way<Integer, Integer> {

        @Override
        protected void run() {
            int i = 1;
            int sum = 0;
            while (i <= 9) {
                int v = yield(i++);
                sum += v;
            }
            int v = yield().call(10);
            sum += v;
            close(sum);
            try {
                yield(11);
                Assert.assertTrue(false);
            }
            catch (Exception ex) {
                Assert.assertTrue(true);
            }

            Assert.assertEquals(385, sum);
        }
    }
}
