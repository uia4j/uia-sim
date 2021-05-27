package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

public class OpEvent extends Event {

    public static final String IDLE = "IDLE";

    public static final String QT_PENDING = "QT_PENDING";

    public static final String PUSH = "PUSH";

    public static final String ENQUEUE = "ENQUEUE";

    public static final String PULL = "PULL";

    private String operation;

    private String job;

    private int queued;

    public OpEvent(String operation, int time, String event, String job, int queued, SimInfo info) {
        super(time, event, info);
        this.operation = operation;
        this.job = job;
        this.queued = queued;
    }

    public OpEvent(String operation, int time, String event, String job, int queued, Map<String, Object> info) {
        super(time, event, info);
        this.operation = operation;
        this.job = job;
        this.queued = queued;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getJob() {
        return this.job;
    }

    public void setJob(String job) {
        this.job = job;
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
                getJob() == null ? "" : getJob());
    }
}
