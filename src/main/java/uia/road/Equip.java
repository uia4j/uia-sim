package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
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

    protected final SimInfo info;

    protected final E10 e10;

    protected String area;

    protected ProcessTimeCalculator<T> processTimeCalculator;

    protected JobSelector<T> jobSelector;

    protected EquipStrategy<T> strategy;

    private Event jobNotifier;

    private boolean enabled;

    private boolean scheduledDown;

    private String status;

    private int timeTag;

    private Vector<String> reserved;

    private int compensationTime;

    private String pushInfo;

    private int lastProcessedTicks;

    private int idleMax;

    private Event idleTimeout;

    /**
     * The constructor.
     *
     * @param id The equipment id.
     * @param factory The factory.
     */
    protected Equip(String id, Factory<T> factory, boolean enabled) {
        super(id);
        this.factory = factory;
        this.operations = new Vector<>();
        this.info = new SimInfo();
        this.e10 = new E10();
        this.jobSelector = new JobSelector.Any<>();
        this.enabled = enabled;
        this.reserved = new Vector<>();
    }

    /**
     * Returns the factory.
     *
     * @return The factory.
     */
    public Factory<T> getFactory() {
        return this.factory;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the operations the equipment handles.
     *
     * @return The operations.
     */
    public List<Op<T>> getOperations() {
        return this.operations;
    }

    public SimInfo getInfo() {
        return this.info;
    }

    public E10 getE10() {
        return this.e10;
    }

    public int getLastProcessedTicks() {
        return this.lastProcessedTicks;
    }

    public void setLastProcessedTicks(int lastProcessedTicks) {
        this.lastProcessedTicks = lastProcessedTicks;
    }

    public int getIdleMax() {
        return this.idleMax;
    }

    public void setIdleMax(int idleMax) {
        this.idleMax = idleMax;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getCompensationTime() {
        return this.compensationTime;
    }

    public void setCompensationTime(int compensationTime) {
        this.compensationTime = Math.max(0, compensationTime);
    }

    public int getReservedNumber() {
        return this.reserved.size();
    }

    public boolean addReserved(Job<T> job) {
        synchronized (this) {
            if (!isEnabled() || !isLoadable(job)) {
                return false;
            }
            this.reserved.add(job.getId());
            return true;
        }
    }

    public boolean removeReserved(Job<T> job) {
        return this.reserved.remove(job.getId());
    }

    public boolean isReserved(Job<T> job) {
        return this.reserved.contains(job.getId());
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

    public String getPushInfo() {
        return this.pushInfo;
    }

    public void setPushInfo(String pushInfo) {
        this.pushInfo = pushInfo;
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

    public abstract int getLoadable();

    /**
     * Tests if the equipment is loadable.
     *
     * @param job A job.
     * @return True if the equipment is loadable.
     */
    public abstract boolean isLoadable(Job<T> job);

    /**
     * Tests if the equipment is idle.
     *
     * @return True if the equipment is idle.
     */
    public abstract boolean isIdle();

    /**
     * Loads a job into the equipment.
     *
     * @param job A job.
     */
    public abstract boolean load(Job<T> job);

    public void close() {
        if (this.jobNotifier != null) {
            this.jobNotifier.envDown();
        }
        if (this.isIdle()) {
            doneStandby();
        }
        else {
            doneProductive();
        }
    }

    public boolean isScheduledUp() {
        return this.scheduledDown;
    }

    public boolean isScheduledDown() {
        if (this.idleMax > 0) {
            return this.scheduledDown || this.factory.ticksNow() >= (this.lastProcessedTicks + this.idleMax);
        }
        else {
            return this.scheduledDown;
        }
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Equip) {
            return getId().equals(((Equip<?>) o).getId());
        }
        else {
            return false;
        }
    }

    protected void logDeny(List<Job<T>> ignore) {
        int now = this.factory.ticksNow();
        for (Job<T> job : ignore) {
            if (job == null) {
                continue;
            }

            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    now,
                    EquipEvent.DENY,
                    job.getOperation(),
                    job.getProductName(),
                    new SimInfo().setString("ignore", job.getDenyInfo())));
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    now,
                    JobEvent.DENY,
                    job.getQty(),
                    job.getOperation(),
                    getId(),
                    0,
                    0,
                    job.getInfo()).deny(job.getDenyCode(), job.getDenyInfo()));
        }
    }

    protected List<String> getJobsInOperations() {
        ArrayList<String> jobs = new ArrayList<>();
        for (Op<T> op : this.operations) {
            op.getEnqueued().forEach(j -> jobs.add(j.getProductName()));
        }
        return jobs;
    }

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

    protected void updateStrategy(Job<T> job, String event) {
        EquipStrategy<T> strategy = getStrategy();
        if (strategy != null) {
            strategy.update(this, job, event);
            job.updateInfo();
        }
    }

    protected int doneStandby() {
        int time = now() - this.timeTag;
        this.timeTag = now();
        this.e10.addStandnbyTime(time);
        return time;
    }

    protected int doneProductive() {
        int time = now() - this.timeTag;
        this.timeTag = now();
        this.e10.addProductiveTime(time);

        //if (this.idleMax > 0) {
        //    this.idleTimeout = this.env().timeout(getId() + "_idle_timeout", this.idleMax);
        //}

        return time;
    }

    @Override
    protected void initial() {
    }
}
