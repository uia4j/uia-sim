package uia.road;

import org.junit.Test;

public class SimInfoTest {

    @Test
    public void test() {
        SimInfo info = new SimInfo();
        info.setValue("test", new Tuple2());
        Tuple2 t = info.get("test");
        System.out.println(t);
    }

    public static class Tuple2 {

        public String id;

        public String value;

    }
}
