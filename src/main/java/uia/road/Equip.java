package uia.road;

import java.util.List;
import java.util.Vector;

import uia.road.helpers.EquipStrategy;
import uia.road.helpers.JobSelector;
import uia.road.helpers.ProcessTimeCalculator;
import uia.sim.Event;
import uia.sim.Processable;

/**
 * The abstract equipment.
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public abstract class Equip<T> extends Processable implements ChannelListener<T> {

    protected final Factory<T> factory;

    protected final Vector<Op<T>> operations;

    protected ProcessTimeCalculator<T> processTimeCalculator;

    protected JobSelector<T> jobSelector;

    protected EquipStrategy<T> strategy;

    private Event jobNotifier;

    private int waitingMaxTime;

    private boolean enabled;

    /**
     * The constructor.
     * 
     * @param id The equipment id.
     * @param factory The factory.
     */
    protected Equip(String id, Factory<T> factory) {
        super(id);
        this.factory = factory;
        this.operations = new Vector<>();
        this.jobSelector = new JobSelector.Any<>();
        this.waitingMaxTime = 300;
        this.enabled = true;
    }

    /**
     * Returns the factory.
     * 
     * @return The factory.
     */
    public Factory<T> getFactory() {
        return this.factory;
    }

    /**
     * Returns the operations the equipment handles.
     * 
     * @return The operations.
     */
    public List<Op<T>> getOperations() {
        return this.operations;
    }

    public ProcessTimeCalculator<T> getProcessTimeCalculator() {
        ProcessTimeCalculator<T> calc = this.processTimeCalculator;
        if (calc == null) {
            return this.factory.getProcessTimeCalculator();
        }
        else {
            return calc;
        }
    }

    public void setProcessTimeCalculator(ProcessTimeCalculator<T> processTimeCalculator) {
        this.processTimeCalculator = processTimeCalculator;
    }

    /**
     * Returns the job selector.
     * 
     * @return The job selector.
     */
    public JobSelector<T> getJobSelector() {
        return this.jobSelector;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the job selector.
     * 
     * @param jobSelector The job selector.
     */
    public void setJobSelector(JobSelector<T> jobSelector) {
        this.jobSelector = jobSelector;
    }

    public EquipStrategy<T> getStrategy() {
        EquipStrategy<T> strategy = this.strategy;
        if (strategy == null) {
            return this.factory.getEquipStrategy();
        }
        else {
            return strategy;
        }
    }

    public void setStrategy(EquipStrategy<T> strategy) {
        this.strategy = strategy;
    }

    public int getWaitingMaxTime() {
        return this.waitingMaxTime;
    }

    public void setWaitingMaxTime(int waitingMaxTime) {
        this.waitingMaxTime = Math.max(10, waitingMaxTime);
    }

    /**
     * Serves the operation.
     * 
     * @param operation The operations.
     */
    public void serve(Op<T> operation) {
        if (operation == null) {
            return;
        }
        if (!this.operations.contains(operation)) {
            this.operations.add(operation);
            operation.serve(this);
        }
    }

    /**
     * Tests if the equipment is loaded.
     * 
     * @return True if the equipment is busy.
     */
    public abstract boolean isLoaded();

    /**
     * Tests if the equipment is idle.
     * 
     * @return True if the equipment is idle.
     */
    public abstract boolean isIdle();

    /**
     * Adds a job.
     * 
     * @param job A job.
     */
    public abstract boolean load(Job<T> job);

    protected void waitingJobs() {
        synchronized (this) {
            if (this.jobNotifier != null) {
                return;
            }
            this.jobNotifier = this.factory.getEnv().event(getId() + "_waiting_jobs");
        }
        yield(this.jobNotifier);
    }

    protected void notifyJobs() {
        synchronized (this) {
            if (this.jobNotifier == null) {
                return;
            }
            this.jobNotifier.succeed(null);
            this.jobNotifier = null;
        }
    }

    @Override
    protected void initial() {
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Equip) {
            return this.getId().equals(((Equip<?>) o).getId());
        }
        else {
            return false;
        }
    }

    protected void updateStrategy(Job<T> job, String event) {
        EquipStrategy<T> strategy = getStrategy();
        if (strategy != null) {
            strategy.update(this, job, event);
            job.updateInfo();
        }
    }
}
