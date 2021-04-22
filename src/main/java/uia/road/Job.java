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

    private final SimInfo info;

    private final Strategy strategy;

    private boolean processing;

    private boolean finished;

    private int dispatchingTime;

    private int dispatchedTime;

    private String moveInEquip;

    private int moveInTime;

    private int moveOutTime;

    private Job<T> prev;

    private Job<T> next;

    private int priority;

    /**
     * Constructor.
     * 
     * @param id The id.
     * @param productName The product name.
     * @param operation The operation.
     * @param data The reference data.
     */
    public Job(String id, String productName, String operation, T data) {
        this.id = id;
        this.productName = productName;
        this.operation = operation;
        this.data = data;
        this.info = new SimInfo();
        this.processing = false;
        this.finished = false;
        this.strategy = new Strategy();
        this.priority = Integer.MAX_VALUE;
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

    public boolean isProcessing() {
        return this.processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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

    public boolean isLoaded() {
        return this.moveInEquip != null;
    }

    public synchronized boolean load(String moveInEquip) {
        if (this.moveInEquip != null && !this.moveInEquip.equals(moveInEquip)) {
            return false;
        }
        this.moveInEquip = moveInEquip;
        return true;
    }

    public Job<T> findNextAt(String operation) {
        Job<T> chk = this;
        while (chk != null && !operation.equals(chk.getOperation())) {
            chk = chk.getNext();
        }
        return chk;
    }

    public Job<T> findPrevAt(String operation) {
        Job<T> chk = this;
        while (chk != null && !operation.equals(chk.getOperation())) {
            chk = chk.getPrev();
        }
        return chk;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    public class Strategy {

        private final TimeStrategy moveIn;

        private final TimeStrategy moveOut;

        public Strategy() {
            this.moveIn = new TimeStrategy();
            this.moveOut = new TimeStrategy();
        }

        public TimeStrategy getMoveIn() {
            return this.moveIn;
        }

        public TimeStrategy getMoveOut() {
            return this.moveOut;
        }

    }
}
