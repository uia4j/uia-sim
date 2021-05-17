package uia.road.utils;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class TimeFormatTest {

    @Test
    public void testFromSec() {
        Assert.assertEquals("  0:01:01", TimeFormat.fromSec(61));
        Assert.assertEquals("  1:01:01", TimeFormat.fromSec(3661));
        Assert.assertEquals(" 23:59:59", TimeFormat.fromSec(86399));
        Assert.assertEquals(" 24:00:00", TimeFormat.fromSec(86400));
    }

    @Test
    public void testFromMin() {
        Assert.assertEquals("  0 01:01", TimeFormat.fromMin(61));
        Assert.assertEquals("  0 23:59", TimeFormat.fromMin(1439));
        Assert.assertEquals("  1 01:01", TimeFormat.fromMin(1501));
    }

    @Test
    public void test() {
        Random rand = new Random();
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
        System.out.println(rand.nextInt(10));
    }
}
