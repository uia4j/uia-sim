package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

public class EquipEvent extends Event {

    public static final String MOVE_IN = "MOVE_IN";

    public static final String PROCESS_START = "PROCESS_START";

    public static final String PROCESSING = "PROCESSING";

    public static final String PROCESS_END = "PROCESS_END";

    public static final String MOVE_OUT = "MOVE_OUT";

    public static final String IDLE_START = "IDLE_START";

    public static final String IDLE_END = "IDLE_END";

    public static final String BUSY = "BUSY";

    public static final String PULL_EMPTY = "PULL_EMPTY";

    public static final String DENY = "DENY";

    private String equip;

    private String ch;

    private String operation;

    private String job;

    private int qty;

    public EquipEvent(String equip, String ch, int time, String event, String operation, String job, SimInfo info) {
        super(time, event, info);
        this.equip = equip;
        this.ch = ch;
        this.operation = operation;
        this.job = job;
    }

    public EquipEvent(String equip, String ch, int time, String event, String operation, String job, int qty, SimInfo info) {
        super(time, event, info);
        this.equip = equip;
        this.ch = ch;
        this.operation = operation;
        this.job = job;
        this.qty = qty;
    }

    public EquipEvent(String equip, String ch, int time, String event, String operation, String job, int qty, Map<String, Object> info) {
        super(time, event, info);
        this.equip = equip;
        this.ch = ch;
        this.operation = operation;
        this.job = job;
        this.qty = qty;
    }

    public String getEquip() {
        return this.equip;
    }

    public void setEquip(String equip) {
        this.equip = equip;
    }

    public String getCh() {
        return this.ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
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

    public int getQty() {
        return this.qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public EquipEvent deny(String code, String info) {
        setDenyCode(code);
        setDenyInfo(info);
        return this;
    }

    @Override
    public String toString() {
        return String.format("%8d %-15s - %s %s",
                this.time,
                this.event,
                this.job == null ? "" : this.job,
                this.ch == null ? "" : this.ch);
    }
}
