package uia.cor;

import org.junit.Assert;
import org.junit.Test;

public class Yieldable2WayTest {

    @Test
    public void test1() {
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

    @Test
    public void test2() throws InterruptedException {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(new CallSum2());
        int i = 0;
        NextResult<Integer> nr;
        while ((nr = gen.nextResult(i * i)).hasNext) {
            i = nr.value.intValue();
            System.out.println("value=" + i);
            if (i == 5) {
                gen.stop(i * i);
            }
        }
        Assert.assertEquals(5, i);
        Assert.assertEquals(55, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void test3() throws InterruptedException {
        Generator2Way<Integer, Integer> gen = Yield2Way.accept(new CallSum2());
        int i = 0;
        NextResult<Integer> nr;
        while ((nr = gen.nextResult(i * i)).hasNext) {
            i = nr.value.intValue();
            System.out.println("value=" + i);
            if (i == 6) {
                gen.stop(new Exception("force stop"));
            }
        }
        Assert.assertEquals(6, i);
        Assert.assertEquals(55, (int) gen.getResult());
        Assert.assertTrue(gen.isClosed());
    }

    public static class CallSum2 extends Yieldable2Way<Integer, Integer> {

        @Override
        protected void run() {
            int i = 1;
            int sum = 0;
            try {
                while (i <= 9) {
                    int v = yield(i++);
                    sum += v;
                }
                int v = yield().call(10);
                sum += v;
                Assert.assertEquals(385, sum);
                close(sum);
            }
            catch (Exception ex) {
                Assert.assertEquals(55, sum);
                close(sum);
            }
        }
    }
}
