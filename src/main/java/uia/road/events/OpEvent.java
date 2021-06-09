package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

public class OpEvent extends Event {

    public static final String IDLE = "IDLE";

    public static final String QT_PENDING = "QT_PENDING";

    public static final String PUSH = "PUSH";

    public static final String ENQUEUE = "ENQUEUE";

    public static final String PULL = "PULL";

    public static final String DENY = "DENY";

    private String operation;

    private String job;

    private String equipment;

    private int queued;

    public OpEvent(String operation, int time, String event, String job, int queued, String equipment, SimInfo info) {
        super(time, event, info);
        this.operation = operation;
        this.job = job;
        this.queued = queued;
        this.equipment = equipment;
    }

    public OpEvent(String operation, int time, String event, String job, int queued, String equipment, Map<String, Object> info) {
        super(time, event, info);
        this.operation = operation;
        this.job = job;
        this.queued = queued;
        this.equipment = equipment;
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

    public String getEquipment() {
        return this.equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public OpEvent deny(String code, String info) {
        setDenyCode(code);
        setDenyInfo(info);
        return this;
    }

    @Override
    public String toString() {
        return String.format("%8d %-15s - %s %s",
                getTime(),
                getEvent(),
                getJob() == null ? "" : getJob(),
                getEquipment() == null ? "" : "@ " + getEquipment());
    }
}
