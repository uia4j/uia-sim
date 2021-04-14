package uia.road.events;

import uia.road.SimInfo;

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

    public static final String DONE = "DONE";

    private String product;

    private String box;

    private String eventTarget;

    private int timeIdled;

    public JobEvent(String product, String box, int time, String event, String eventTarget, int timeIdled, SimInfo info) {
        super(time, event, info);
        this.product = product;
        this.box = box;
        this.eventTarget = eventTarget;
        this.timeIdled = timeIdled;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getBox() {
        return this.box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getEventTarget() {
        return this.eventTarget;
    }

    public void setEventTarget(String eventTarget) {
        this.eventTarget = eventTarget;
    }

    public int getTimeIdled() {
        return this.timeIdled;
    }

    public void setTimeIdled(int timeIdled) {
        this.timeIdled = timeIdled;
    }

    @Override
    public String toString() {
        return String.format("%8d %-15s - %s",
                getTime(),
                getEvent(),
                getEventTarget() == null ? "" : getEventTarget());
    }
}
