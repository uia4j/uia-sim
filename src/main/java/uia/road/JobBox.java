package uia.road;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A job collection.
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class JobBox<T> {

    private final String id;

    private final String operation;

    private final List<Job<T>> jobs;

    private SimInfo info;

    private int dispatchingTime;

    private int dispatchedTime;

    private int moveInTime;

    private int moveOutTime;

    private boolean dispatched;

    /**
     * Constructor.
     * 
     * @param id The box id.
     * @param operation The operation.
     */
    public JobBox(String id, String operation) {
        this.id = id;
        this.operation = operation;
        this.info = new SimInfo();
        this.jobs = new ArrayList<>();
        this.dispatched = false;
    }

    /**
     * Constructor.
     * 
     * @param id The box id.
     * @param operation The operation.
     * @param job The job.
     */
    public JobBox(String id, String operation, Job<T> job) {
        this(id, operation, Arrays.asList(job));
    }

    /**
     * Constructor.
     * 
     * @param id The box id.
     * @param operation The operation.
     * @param job The job collection.
     */
    public JobBox(String id, String operation, List<Job<T>> jobs) {
        this.id = id;
        this.operation = operation;
        this.info = new SimInfo();
        this.jobs = jobs;
        this.jobs.forEach(j -> j.setBoxId(id));
    }

    /**
     * Returns the box id.
     * 
     * @return The box id.
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
     * Returns the extra information. The information will be logged.
     * 
     * @return The extra information.
     */
    public SimInfo getInfo() {
        return this.info;
    }

    /**
     * Sets the extra information. The information will be logged.
     * 
     * @param info The extra information.
     */
    public void setInfo(SimInfo info) {
        this.info = info;
    }

    /**
     * Adds a job in the box.
     * 
     * @param job The new job.
     */
    public void addJob(Job<T> job) {
        job.setBoxId(this.id);
        this.jobs.add(job);
    }

    /**
     * Returns jobs in the box.
     * 
     * @return The job collection.
     */
    public List<Job<T>> getJobs() {
        return this.jobs;
    }

    /**
     * Tests if the box is empty.
     * 
     * @return True if the box is empty.
     */
    public boolean isEmpty() {
        return this.jobs.isEmpty();
    }

    /**
     * Tests if the box is processing
     * 
     * @return True if any one of jobs  is processing.
     */
    public boolean isProcessing() {
        return this.jobs.stream().filter(j -> j.isProcessing()).findAny().isPresent();
    }

    /**
     * Test if the box is processed.
     * 
     * @return True if all jobs in the box are processed.
     */
    public boolean isFinished() {
        return !this.jobs.stream().filter(j -> !j.isFinished()).findAny().isPresent();
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
        this.jobs.forEach(j -> j.setDispatchingTime(dispatchingTime));
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
        if (!this.dispatched) {
            this.dispatched = true;
            this.dispatchedTime = dispatchedTime;
            this.jobs.forEach(j -> j.setDispatchedTime(dispatchedTime));
        }
    }

    public int getMoveInTime() {
        return this.moveInTime;
    }

    public void setMoveInTime(int moveInTime) {
        this.moveInTime = moveInTime;
        this.jobs.forEach(j -> j.setMoveInTime(moveInTime));
    }

    public int getMoveOutTime() {
        return this.moveOutTime;
    }

    public void setMoveOutTime(int moveOutTime) {
        this.moveOutTime = moveOutTime;
        this.jobs.forEach(j -> j.setMoveOutTime(moveOutTime));
    }

    public TimeStrategy calcMoveInStrategy() {
        int after = 0;
        int before = Integer.MAX_VALUE;
        for (Job<T> j : this.jobs) {
            after = Math.max(after, j.getStrategy().getMoveIn().getFrom());
            before = Math.min(before, j.getStrategy().getMoveIn().getTo());
        }
        return new TimeStrategy(after, before);
    }

    public TimeStrategy calcMoveOutStrategy() {
        int after = 0;
        int before = Integer.MAX_VALUE;
        for (Job<T> j : this.jobs) {
            after = Math.max(after, j.getStrategy().getMoveOut().getFrom());
            before = Math.min(before, j.getStrategy().getMoveOut().getTo());
        }
        return new TimeStrategy(after, before);
    }

    @Override
    public String toString() {
        return String.format("%s @ %s",
                this.id,
                this.operation);
    }
}
