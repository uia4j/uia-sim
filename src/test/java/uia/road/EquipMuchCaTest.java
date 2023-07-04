package uia.road;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import uia.road.helpers.ProcessTimeCalculator.TimeInfo;

public class EquipMuchCaTest implements ChannelSelector<Integer> {

    @Test
    public void test1() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> TimeInfo.create(j.getData().intValue()));

        EquipMuchCa<Integer> e1 = new EquipMuchCa<>("e1", factory, 2, 2, true);
        e1.getChannels().get(0).getInfo().setString("p", "p1");
        e1.getChannels().get(1).getInfo().setString("p", "p2");
        e1.setChSelector(this);
        factory.tryAddEquip(e1);

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        o1.serve(e1);

        Job<Integer> p1 = new Job<>("1", "p1", o1.getId(), 1, null, 90);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("1", "p2", o1.getId(), 1, null, 80);
        p2.setQty(7);

        factory.prepare(p1);
        factory.prepare(p2);

        factory.run(2000);
        Assert.assertEquals(0, e1.getLoaded());

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Test
    public void test2() throws Exception {
        /**
         * o1
         * e1
         */
        Factory<Integer> factory = new Factory<>();
        factory.setProcessTimeCalculator((e, j) -> TimeInfo.create(j.getData().intValue()));

        EquipMuchCa<Integer> e1 = new EquipMuchCa<>("e1", factory, 2, 2, true);
        e1.getChannels().get(0).getInfo().setString("p", "p1");
        e1.getChannels().get(1).getInfo().setString("p", "p1");
        e1.setChSelector(this);
        factory.tryAddEquip(e1);

        Op<Integer> o1 = factory.tryCreateOperation("o1");
        o1.serve(e1);

        Job<Integer> p1 = new Job<>("1", "p1", o1.getId(), 1, null, 100);
        p1.setQty(5);
        Job<Integer> p2 = new Job<>("1", "p2", o1.getId(), 1, null, 100);
        p2.setQty(7);

        factory.prepare(p1);
        factory.prepare(p2);

        factory.run(2000);
        factory.getLogger().printlnSimpleEquipEvents();
        factory.getLogger().printlnSimpleJobEvents();
        Assert.assertEquals(1, e1.getLoaded());

        factory.getLogger().printlnSimpleEquipEvents();
    }

    @Override
    public Channel<Integer> select(List<Channel<Integer>> chs) {
        return select(chs, null);
    }

    @Override
    public Channel<Integer> select(List<Channel<Integer>> chs, Job<Integer> job) {
        if (job == null) {
            return null;
        }

        Optional<Channel<Integer>> opts = chs.stream()
                .filter(c -> !c.isProcessing())
                .filter(c -> job.getProductName().equals(c.getInfo().get("p")))
                .findAny();
        return opts.isPresent() ? opts.get() : null;
    }
}
