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

    private final String operation;

    private final T data;

    private final SimInfo info;

    private final Strategy strategy;

    private String boxId;

    private boolean processing;

    private boolean finished;

    private int dispatchingTime;

    private int dispatchedTime;

    private int moveInTime;

    private int moveOutTime;

    private Job<T> prev;

    private Job<T> next;

    /**
     * Constructor.
     * 
     * @param id The job id.
     * @param operation The operation.
     * @param data The reference data.
     */
    public Job(String id, String operation, T data) {
        this(id, id, operation, data);
    }

    /**
     * Constructor.
     * 
     * @param id The job id.
     * @param boxId The box id.
     * @param operation The operation.
     * @param data The reference data.
     */
    public Job(String id, String boxId, String operation, T data) {
        this.id = id;
        this.boxId = boxId == null ? this.id : boxId;
        this.operation = operation;
        this.data = data;
        this.info = new SimInfo();
        this.processing = false;
        this.finished = false;
        this.strategy = new Strategy();
    }

    /**
     * Returns the job id.
     * 
     * @return The job id.
     */
    public String getId() {
        return this.id;
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

    public void setPrev(Job<T> prev) {
        this.prev = prev;
    }

    public Job<T> getNext() {
        return this.next;
    }

    public void setNext(Job<T> next) {
        this.next = next;
    }

    public String getBoxId() {
        return this.boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    /**
     * Returns the time dispatching to the operation.
     * 
     * @return The time.
     */
    public int getDispatchingTime() {
        return this.dispatchingTime;
    }

    /**
     * Sets the time dispatching to the operation.
     * 
     * @param dispatchingTime The time.
     */
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

    /**
     * Sets the arrival time at the operation.
     * 
     * @param dispatchedTime The time.
     */
    public void setDispatchedTime(int dispatchedTime) {
        this.dispatchedTime = dispatchedTime;
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

    public void updateInfo() {
        this.info.getInfo("moveIn").setInt("from", this.strategy.getMoveIn().getFrom());
        this.info.getInfo("moveIn").setInt("to", this.strategy.getMoveIn().getTo());
        this.info.getInfo("moveOut").setInt("from", this.strategy.getMoveOut().getFrom());
        this.info.getInfo("moveOut").setInt("to", this.strategy.getMoveOut().getTo());
    }

    @Override
    public String toString() {
        return String.format("%s @ %s",
                this.id,
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
