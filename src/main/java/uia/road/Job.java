package uia.road;

/**
 *
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Job<T> {

    private final String id;

    private final String productName;

    private final String operation;

    private final T data;

    private String area;

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

    private String denyCode;

    private String denyInfo;

    private int index;

    /**
     * Constructor.
     *
     * @param id The id.
     * @param productName The product name.
     * @param operation The operation.
     * @param priority The priority.
     * @param data The reference data.
     */
    public Job(String id, String productName, String operation, int priority, T data) {
        this.id = id;
        this.productName = productName;
        this.operation = operation;
        this.data = data;
        this.info = new SimInfo();
        this.strategy = new Strategy();
        this.qty = 1;
        this.engineering = false;
        this.priority = priority;
        this.denyInfo = "OK";
    }

    /**
     * Constructor.
     *
     * @param job The job
     */
    public Job(Job<T> job) {
        this.id = job.id;
        this.productName = job.productName;
        this.operation = job.operation;
        this.data = job.data;
        this.area = job.area;
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
        this.denyCode = job.denyCode;
        this.denyInfo = job.denyInfo;
        this.index = job.index;
    }

    public String getId() {
        return this.id;
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

    public String getDenyCode() {
        return this.denyCode;
    }

    public void setDenyCode(String denyCode) {
        this.denyCode = denyCode;
    }

    public String getDenyInfo() {
        return this.denyInfo;
    }

    public void setDenyInfo(String denyInfo) {
        this.denyInfo = denyInfo;
    }

    public synchronized int processing(int qty) {
        int r = this.qty - this.processingQty;
        this.processingQty += qty;
        if (this.processedQty >= this.qty) {
            this.processedQty = this.qty;
            return r;
        }
        else {
            return qty;
        }

    }

    public synchronized void processed(int qty) {
        this.processedQty += qty;
        if (this.processedQty >= this.qty) {
            this.processedQty = this.qty;
        }
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

    public Job<T> findNext(String id, boolean self) {
        if (self && id.equals(this.id)) {
            return this;
        }
        if (this.next == null) {
            return null;
        }
        return this.next.findNext(id, true);
    }

    public Job<T> last() {
        return this.next == null ? this : this.next.last();
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
