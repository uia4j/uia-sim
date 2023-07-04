package uia.road;

import java.util.function.Function;

import uia.sim.Processable;

/**
 *
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Job<T> {

    private final String id;

    private final int hotLevel;

    private final String hotReason;

    private final String productName;

    private final String operation;

    private final T data;

    private final SimInfo info;

    private final Strategy strategy;

    private int dispatchingTime;

    private int dispatchedTime;

    private String moveInEquip;

    private int moveInTime;

    private int moveOutTime;

    private Job<T> prev;

    private Job<T> next;

    private int qty;

    private int processingQty;

    private int processedQty;

    private boolean engineering;

    private int priority;

    private String priorityInfo;

    private String denyCode;

    private int denyCount;

    private int index;

    private int predictProcessTime;

    private boolean running;

    private int seqNo;

    private String area;

    /**
     * Constructor.
     *
     * @param id The id.
     * @param productName The product name.
     * @param operation The operation.
     * @param hotLevel The hot level.
     * @param data The reference data.
     */
    public Job(String id, String productName, String operation, int hotLevel, T data) {
        this(id, productName, operation, hotLevel, null, data);
    }

    /**
     * Constructor.
     *
     * @param id The id.
     * @param productName The product name.
     * @param operation The operation.
     * @param hotLevel The hot level.
     * @param hotReason The hot reason.
     * @param data The reference data.
     */
    public Job(String id, String productName, String operation, int hotLevel, String hotReason, T data) {
        this.id = id;
        this.hotLevel = hotLevel;
        this.hotReason = hotReason;
        this.productName = productName;
        this.operation = operation;
        this.data = data;
        this.info = new SimInfo();
        this.strategy = new Strategy();
        this.qty = 1;
        this.engineering = false;
        this.priority = hotLevel;
        this.priorityInfo = hotReason;
        this.running = false;
    }

    /**
     * Constructor.
     *
     * @param job The job
     */
    public Job(Job<T> job) {
        this.id = job.id;
        this.hotLevel = job.hotLevel;
        this.hotReason = job.hotReason;
        this.productName = job.productName;
        this.operation = job.operation;
        this.data = job.data;
        this.info = job.info;
        this.strategy = job.strategy;
        this.dispatchingTime = job.dispatchingTime;
        this.dispatchedTime = job.dispatchedTime;
        this.moveInEquip = job.moveInEquip;
        this.moveInTime = job.moveInTime;
        this.moveOutTime = job.moveOutTime;
        this.qty = job.qty;
        this.processingQty = job.processingQty;
        this.processedQty = job.processedQty;
        this.priority = job.priority;
        this.priorityInfo = job.priorityInfo;
        this.denyCode = job.denyCode;
        this.denyCount = job.denyCount;
        this.index = job.index;
        this.predictProcessTime = job.predictProcessTime;
        this.running = job.running;
        this.seqNo = job.seqNo;
        this.area = job.area;
    }

    public String getId() {
        return this.id;
    }

    public int getHotLevel() {
        return this.hotLevel;
    }

    public String getHotReason() {
        return this.hotReason;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Returns the product name.
     *
     * @return The product name.
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * Returns the operation.
     *
     * @return The operation id.
     */
    public String getOperation() {
        return this.operation;
    }

    /**
     * Return the reference data.
     *
     * @return The reference data.
     */
    public T getData() {
        return this.data;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean forceDispatch() {
        return this.denyCount >= 20;
    }

    public void deny() {
        this.denyCount++;
    }

    /**
     * Returns the extra information. The information will be logged.
     *
     * @return The extra information.
     */
    public SimInfo getInfo() {
        return this.info;
    }

    public Strategy getStrategy() {
        return this.strategy;
    }

    public boolean isFinished() {
        return this.processedQty >= this.qty;
    }

    public Job<T> getPrev() {
        return this.prev;
    }

    public Job<T> setPrev(Job<T> prev) {
        if (this.prev != prev) {
            this.prev = prev;
            if (prev != null) {
                this.prev.setNext(this);
            }
        }
        return this.prev;
    }

    public Job<T> getNext() {
        return this.next;
    }

    public Job<T> setNext(Job<T> next) {
        if (this.next != next) {
            this.next = next;
            if (next != null) {
                next.setPrev(this);
            }
        }
        return this.next;
    }

    /**
     * Returns the time dispatching to the operation.
     *
     * @return The time.
     */
    public int getDispatchingTime() {
        return this.dispatchingTime;
    }

    public void setDispatchingTime(int dispatchingTime) {
        this.dispatchingTime = dispatchingTime;
    }

    /**
     * Returns the arrival time at the operation.
     *
     * @return The time.
     */
    public int getDispatchedTime() {
        return this.dispatchedTime;
    }

    public void setDispatchedTime(int dispatchedTime) {
        this.dispatchedTime = dispatchedTime;
    }

    public String getMoveInEquip() {
        return this.moveInEquip;
    }

    public void setMoveInEquip(String moveInEquip) {
        this.moveInEquip = moveInEquip;
    }

    public int getMoveInTime() {
        return this.moveInTime;
    }

    public void setMoveInTime(int moveInTime) {
        this.moveInTime = moveInTime;
    }

    public int getMoveOutTime() {
        return this.moveOutTime;
    }

    public void setMoveOutTime(int moveOutTime) {
        this.moveOutTime = moveOutTime;
    }

    public int getQty() {
        return this.qty;
    }

    public void setQty(int qty) {
        this.qty = Math.max(1, qty);
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isEngineering() {
        return this.engineering;
    }

    public void setEngineering(boolean engineering) {
        this.engineering = engineering;
    }

    public int getProcessingQty() {
        return this.processingQty;
    }

    public int getProcessedQty() {
        return this.processedQty;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPriorityInfo() {
        return this.priorityInfo;
    }

    public void setPriorityInfo(String priorityInfo) {
        this.priorityInfo = priorityInfo;
    }

    public String getDenyCode() {
        return this.denyCode;
    }

    public void setDenyCode(String denyCode) {
        this.denyCode = denyCode;
    }

    //public String getDenyInfo() {
    //    return this.priorityInfo == null ? "OK" : this.priorityInfo;
    //}

    public int getPredictProcessTime() {
        return this.predictProcessTime;
    }

    public void setPredictProcessTime(int predictProcessTime) {
        this.predictProcessTime = predictProcessTime;
    }

    public synchronized int processing(int qty) {
        int r = this.qty - this.processingQty;
        if (qty <= 0 || qty >= r) {
            this.processingQty = this.qty;
            return r;
        }
        else {
            this.processingQty += qty;
            return qty;
        }

    }

    public synchronized void processed(int qty) {
        this.processedQty = qty <= 0
                ? this.qty
                : Math.min(this.processedQty + qty, this.qty);
    }

    public boolean isLoaded() {
        return this.moveInEquip != null;
    }

    public Job<T> findNextAt(String operation) {
        if (this.next == null) {
            return null;
        }
        if (operation.equals(this.next.getOperation())) {
            return this.next;
        }
        return this.next.findNextAt(operation);
    }

    public Job<T> findPrevAt(String operation) {
        if (this.prev == null) {
            return null;
        }
        if (operation.equals(this.prev.getOperation())) {
            return this.prev;
        }
        return this.prev.findPrevAt(operation);
    }

    public Job<T> findNextJob(String id, boolean self) {
        if (self && id.equals(this.id)) {
            return this;
        }
        if (this.next == null) {
            return null;
        }
        return this.next.findNextJob(id, true);
    }

    public Job<T> findPrevJob(String id, boolean self) {
        if (self && id.equals(this.id)) {
            return this;
        }
        if (this.prev == null) {
            return null;
        }
        return this.prev.findPrevJob(id, true);
    }

    public int getSeqNo() {
        return this.seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public Job<T> findNext(Function<Job<T>, Boolean> finder, boolean self) {
        if (self && finder.apply(this)) {
            return this;
        }
        if (this.next == null) {
            return null;
        }
        if (finder.apply(this.next)) {
            return this.next;
        }
        return this.next.findNext(finder, true);
    }

    public Job<T> findPrev(Function<Job<T>, Boolean> finder, boolean self) {
        if (self && finder.apply(this)) {
            return this;
        }
        if (this.prev == null) {
            return null;
        }
        if (finder.apply(this.prev)) {
            return this.prev;
        }
        return this.prev.findPrev(finder, true);
    }

    public Job<T> last() {
        return this.next == null ? this : this.next.last();
    }

    public void rebase() {
        this.priority = this.hotLevel;
        this.priorityInfo = this.hotReason;
        this.denyCode = null;
    }

    public void updateInfo() {
        this.info.getInfo("moveIn").setInt("from", this.strategy.getMoveIn().getFrom());
        this.info.getInfo("moveIn").setInt("to", this.strategy.getMoveIn().getTo());
        this.info.getInfo("moveOut").setInt("from", this.strategy.getMoveOut().getFrom());
        this.info.getInfo("moveOut").setInt("to", this.strategy.getMoveOut().getTo());
        if (this.next != null) {
            this.next.updateInfo();
        }
    }

    @Override
    public String toString() {
        return String.format("%s @ %s",
                this.productName,
                this.operation);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Job) {
            return this.id.equals(((Job<?>) o).getId());
        }
        else {
            return false;
        }
    }

    public class Jump extends Processable {

        private final Factory<T> factory;

        private final int after;

        protected Jump(Factory<T> factory, int after) {
            super(Job.this.getId() + "_jump");
            this.factory = factory;
            this.after = after;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.after));
            this.factory.dispatchToNext(Job.this);
        }

        @Override
        protected void initial() {
        }
    }

    public class Strategy {

        private String assignedEquip;

        private final TimeStrategy moveIn;

        private final TimeStrategy moveOut;

        public Strategy() {
            this.moveIn = new TimeStrategy();
            this.moveOut = new TimeStrategy();
        }

        public String getAssignedEquip() {
            return this.assignedEquip;
        }

        public void setAssignedEquip(String assignedEquip) {
            this.assignedEquip = assignedEquip;
        }

        public boolean acceptEquip(String equipId) {
            return this.assignedEquip == null || this.assignedEquip.equals(equipId);
        }

        public TimeStrategy getMoveIn() {
            return this.moveIn;
        }

        public TimeStrategy getMoveOut() {
            return this.moveOut;
        }

    }
}
