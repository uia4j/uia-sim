package uia.road.eq;

import uia.road.Equip;
import uia.sim.Processable;

public class DownCmd<T> extends Processable {

    protected DownCmd() {
        super("Down");
        // TODO Auto-generated constructor stub
    }

    private Equip<T> eq;

    @Override
    protected void initial() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void run() {
        yield(env().timeout(60));
    }

}
