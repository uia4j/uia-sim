package uia.road.events;

import uia.road.SimInfo;

public class OpEvent extends Event {

    public static final String QT_PENDING = "QT_PENDING";

    public static final String ENQUEUE = "ENQUEUE";

    public static final String DEQUEUE = "DEQUEUE";

    public static final String HOLD = "HOLD";

    public static final String SPLIT = "SPLIT";

    private String operation;

    private String box;

    private int queued;

    public OpEvent(String operation, int time, String event, String box, int queued, SimInfo info) {
        super(time, event, info);
        this.operation = operation;
        this.box = box;
        this.queued = queued;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getBox() {
        return this.box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public int getQueued() {
        return this.queued;
    }

    public void setQueued(int queued) {
        this.queued = queued;
    }

    @Override
    public String toString() {
        return String.format("%8d %-15s - %s",
                getTime(),
                getEvent(),
                getBox());
    }
}
