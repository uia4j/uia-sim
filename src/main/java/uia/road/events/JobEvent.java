package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

/**
 * The event on the job.
 *
 * @author Kan
 *
 */
public class JobEvent extends Event {

    public static final String MOVE_IN = "MOVE_IN";

    public static final String PROCESS_START = "PROCESS_START";

    public static final String PROCESSING = "PROCESSING";

    public static final String PROCESS_END = "PROCESS_END";

    public static final String MOVE_OUT = "MOVE_OUT";

    public static final String DISPATCHING = "DISPATCHING";

    public static final String DISPATCHED = "DISPATCHED";

    public static final String QT_PENDING = "QT_PENDING";

    public static final String HOLD = "HOLD";

    public static final String QT_HOLD = "QT_HOLD";

    public static final String DONE = "DONE";

    public static final String FORCE_JUMP = "FORCE_JUMP";

    public static final String DENY = "DENY";

    private String id;

    private String product;

    private String operation;

    private String equipment;

    private int timeIdled;

    private int timeDispatching;

    private int timeProcessed;

    private int qty;

    public JobEvent(String id, String product, int time, String event, int qty, String operation, String equipment, int timeIdled, SimInfo info) {
        super(time, event, info);
        this.id = id;
        this.product = product;
        this.operation = operation;
        this.equipment = equipment;
        this.timeIdled = timeIdled;
        this.timeProcessed = 0;
        this.qty = qty;
    }

    public JobEvent(String id, String product, int time, String event, int qty, String operation, String equipment, int timeIdled, int timeProcessed, SimInfo info) {
        super(time, event, info);
        this.id = id;
        this.product = product;
        this.operation = operation;
        this.equipment = equipment;
        this.timeIdled = timeIdled;
        this.timeProcessed = timeProcessed;
        this.qty = qty;
    }

    public JobEvent(String id, String product, int time, String event, int qty, String operation, String equipment, int timeIdled, int timeProcessed, Map<String, Object> info) {
        super(time, event, info);
        this.id = id;
        this.product = product;
        this.operation = operation;
        this.equipment = equipment;
        this.timeIdled = timeIdled;
        this.timeProcessed = timeProcessed;
        this.qty = qty;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getEquipment() {
        return this.equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public int getTimeIdled() {
        return this.timeIdled;
    }

    public void setTimeIdled(int timeIdled) {
        this.timeIdled = timeIdled;
    }

    public int getTimeProcessed() {
        return this.timeProcessed;
    }

    public void setTimeProcessed(int timeProcessed) {
        this.timeProcessed = timeProcessed;
    }

    public int getQty() {
        return this.qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getTimeDispatching() {
        return this.timeDispatching;
    }

    public void setTimeDispatching(int timeDispatching) {
        this.timeDispatching = timeDispatching;
    }

    public JobEvent deny(String code, String info) {
        setDenyCode(code);
        setDenyInfo(info);
        return this;
    }

    @Override
    public String toString() {
        if (this.equipment == null) {
            return String.format("%8d %-15s - %-45s",
                    this.time,
                    this.event,
                    this.operation == null ? "" : this.operation);
        }
        else {
            return String.format("%8d %-15s - %-45s, eq:%s",
                    this.time,
                    this.event,
                    this.operation,
                    this.equipment);
        }
    }
}
