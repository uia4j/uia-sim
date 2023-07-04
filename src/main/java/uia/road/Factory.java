package uia.road;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.EquipStrategy;
import uia.road.helpers.PathTimeCalculator;
import uia.road.helpers.ProcessTimeCalculator;
import uia.road.utils.TimeFormat;
import uia.sim.Env;

/**
 * The factory.<br>
 *
 * <p>
 * The default values:<br>
 * 1. pathTime is 0.<br>
 * 2. timeFactor is 1000.<br>
 * </p>
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Factory<T> {

    public enum TimeType {

        SEC(1000, TimeFormat::fromSec),

        MIN(60000, TimeFormat::fromMin);

        public final int factor;

        private final Function<Integer, String> format;

        TimeType(int factor, Function<Integer, String> format) {
            this.factor = factor;
            this.format = format;
        }

        public String format(int ticks) {
            return this.format.apply(ticks);
        }
    }

    private final Env env;

    private final TreeMap<String, Op<T>> operations;

    private final TreeMap<String, Equip<T>> equips;

    private SimReportLogger logger;

    private ProcessTimeCalculator<T> processTimeCalculator;

    private EquipStrategy<T> equipStrategy;

    private PathTimeCalculator<T> pathTimeCalculator;

    private Date zeroTime;

    private TimeType timeType;

    private DispatchStrategy<T> dispStrategy;

    private TreeSet<String> products;

    public Factory() {
        this(0);
    }

    public Factory(int defaultPathTime) {
        this.env = new Env();
        this.env.setCheckPoint(1000);
        this.operations = new TreeMap<>();
        this.equips = new TreeMap<>();
        this.logger = new SimReportTextLogger(this);
        this.pathTimeCalculator = new PathTimeCalculator.Simple<T>(defaultPathTime);
        this.timeType = TimeType.SEC;
        this.zeroTime = new Date();
        this.dispStrategy = new DispatchStrategy.Same<>();
        this.products = new TreeSet<>();
    }

    public Env getEnv() {
        return this.env;
    }

    public Date getZeroTime() {
        return this.zeroTime;
    }

    public void setZeroTime(Date zeroTime) {
        this.zeroTime = zeroTime;
    }

    public TimeType getTimeType() {
        return this.timeType;
    }

    public void setTimeType(TimeType timeType) {
        this.timeType = timeType;
    }

    public int getCountOfOperations() {
        return this.operations.size();
    }

    public int getCountOfEquipments() {
        return this.equips.size();
    }

    public boolean isIdle() {
        boolean idle = !this.equips.values().stream()
                .filter(e -> !e.isIdle())
                .findAny()
                .isPresent();
        if (idle) {
            idle = !this.operations.values().stream()
                    .filter(o -> o.getEnqueued().size() > 0)
                    .findAny()
                    .isPresent();
        }
        return idle;
    }

    /**
     * Sets the logger.
     *
     * @param logger The logger.
     */
    public void setLogger(SimReportLogger logger) {
        this.logger = logger;
    }

    /**
     * Returns the report logger.
     *
     * @return The report logger.
     */
    public SimReportLogger getLogger() {
        return this.logger;
    }

    public ProcessTimeCalculator<T> getProcessTimeCalculator() {
        return this.processTimeCalculator;
    }

    public void setProcessTimeCalculator(ProcessTimeCalculator<T> processTimeCalculator) {
        this.processTimeCalculator = processTimeCalculator;
    }

    public EquipStrategy<T> getEquipStrategy() {
        return this.equipStrategy;
    }

    public void setEquipStrategy(EquipStrategy<T> equipStrategy) {
        this.equipStrategy = equipStrategy;
    }

    public PathTimeCalculator<T> getPathTimeCalculator() {
        return this.pathTimeCalculator;
    }

    public void setPathTimeCalculator(PathTimeCalculator<T> pathTimeCalculator) {
        this.pathTimeCalculator = pathTimeCalculator;
    }

    public DispatchStrategy<T> getDispStrategy() {
        return this.dispStrategy;
    }

    public void setDispStrategy(DispatchStrategy<T> dispStrategy) {
        this.dispStrategy = dispStrategy;
    }

    /**
     * Logs a an operation event.
     *
     * @param e An operation event.
     */
    public void log(OpEvent e) {
        this.logger.log(e);
    }

    /**
     * Logs a equipment event.
     *
     * @param e An equipment event.
     */
    public void log(EquipEvent e) {
        this.logger.log(e);
    }

    /**
     * Logs a job event.
     *
     * @param e A job event.
     */
    public void log(JobEvent e) {
        this.logger.log(e);
    }

    /**
     * Returns an operation in the factory.
     *
     * @param id The operation id.
     * @return The operation.
     */
    public Op<T> getOperation(String id) {
        return id == null
                ? null
                : this.operations.get(id);
    }

    public Set<String> getOperations() {
        return new TreeSet<>(this.operations.keySet());
    }

    public Set<String> getEquips() {
        return new TreeSet<>(this.equips.keySet());
    }

    /**
     * Returns an equipment in the factory.
     *
     * @param id The equipment id.
     * @return The equipment.
     */
    public Equip<T> getEquip(String id) {
        return id == null
                ? null
                : this.equips.get(id);
    }

    /**
     * Creates an operation and add into this factory.
     *
     * @param id The operation id.
     * @return The operation.
     */
    public Op<T> tryCreateOperation(String id) {
        synchronized (this.operations) {
            Op<T> op = this.operations.get(id);
            if (op == null) {
                op = new Op<>(id, this);
                this.operations.put(op.getId(), op);
            }
            return op;
        }
    }

    /**
     * Adds an operation in the factory.
     *
     * @param op The operation.
     * @return True if the operation is added into this factory.
     */
    public Op<T> tryAddOperation(Op<T> op) {
        synchronized (this.operations) {
            Op<T> selected = this.operations.get(op.getId());
            if (selected == null) {
                selected = op;
                this.operations.put(op.getId(), op);
            }
            return selected;
        }
    }

    /**
     * Creates an equipment and add into this factory.
     *
     * @param id The equipment id.
     * @param loadPorts Number of load port.
     * @param chCount Number of the channel.
     * @return The equipment.
     */
    public Equip<T> tryCreateEquip(String id, int loadPorts, int chCount) {
        return tryCreateEquip(id, loadPorts, chCount, true);
    }

    /**
     * Creates an equipment and add into this factory.
     *
     * @param id The equipment id.
     * @param loadPorts Number of load port.
     * @param chCount Number of the channel.
     * @param enabled Enabled or not.
     * @return The equipment.
     */
    public Equip<T> tryCreateEquip(String id, int loadPorts, int chCount, boolean enabled) {
        synchronized (this.equips) {
            Equip<T> eq = this.equips.get(id);
            if (eq == null) {
                eq = new EquipMuch<>(id, this, loadPorts, chCount, enabled);
                this.equips.put(eq.getId(), eq);
            }
            return eq;
        }
    }

    /**
     * Adds an equipment in the factory.
     *
     * @param equip The equipment.
     * @return True if the equipment is added into this factory.
     */
    public Equip<T> tryAddEquip(Equip<T> equip) {
        if (equip == null) {
            return null;
        }

        synchronized (this.equips) {
            Equip<T> selected = this.equips.get(equip.getId());
            if (selected == null) {
                selected = equip;
                this.equips.put(equip.getId(), equip);
            }
            return selected;
        }
    }

    /**
     * Prepares a job to its operation.
     *
     * @param job The job.
     */
    public void prepare(Job<T> job) {
        Op<T> op = null;
        synchronized (this.operations) {
            op = this.operations.get(job.getOperation());
            if (op == null) {
                op = new Op<T>(job.getOperation(), this);
                this.operations.put(op.getId(), op);
            }
            op.enqueue(job, false);
        }
        this.products.add(job.getProductName());
    }

    /**
     * Prepares a job to its operation.
     *
     * @param job The job.
     */
    public void prepareNoDelay(Job<T> job) {
        Op<T> op = null;
        synchronized (this.operations) {
            op = this.operations.get(job.getOperation());
            if (op == null) {
                op = new Op<T>(job.getOperation(), this);
                this.operations.put(op.getId(), op);
            }
        }
        this.products.add(job.getProductName());
        op.enqueueNoDelay(job, false);
    }

    /**
     * Dispatches a job to its operation and run.
     *
     * @param job The job.
     */
    public void dispatch(Job<T> job) {
        Op<T> op = null;
        synchronized (this.operations) {
            op = this.operations.get(job.getOperation());
            if (op == null) {
                op = new Op<T>(job.getOperation(), this);
                this.operations.put(op.getId(), op);
            }
        }
        this.products.add(job.getProductName());
        op.enqueue(job, true);
    }

    /**
     * Dispatches a job to its operation and run.
     *
     * @param job The job.
     */
    public void dispatchNoDelay(Job<T> job) {
        Op<T> op = null;
        synchronized (this.operations) {
            op = this.operations.get(job.getOperation());
            if (op == null) {
                op = new Op<T>(job.getOperation(), this);
                this.operations.put(op.getId(), op);
            }
        }
        this.products.add(job.getProductName());
        op.enqueueNoDelay(job, true);
    }

    /**
     * Loads a job into a specific equipment.
     * @param eqId The equipment id.
     * @param job The job.
     * @return True if the job is loaded into the equipment.
     */
    public boolean dispatch(Job<T> job, String eqId) {
        Equip<T> eq = this.equips.get(eqId);
        if (eq == null) {
            return false;
        }
        this.products.add(job.getProductName());
        return eq.load(job);
    }

    /**
     * Dispatches the job to next operation.
     *
     * @param doneJ The job
     */
    public void dispatchToNext(Job<T> doneJ) {
        this.dispStrategy.replan(doneJ);
        Job<T> nextJ = doneJ.getNext();
        if (nextJ == null) {
            this.products.remove(doneJ.getProductName());
            this.log(new JobEvent(
                    doneJ.getId(),
                    doneJ.getProductName(),
                    ticksNow(),
                    JobEvent.DONE,
                    doneJ.getQty(),
                    null,
                    null,
                    0,
                    doneJ.getInfo()));
            return;
        }

        int pathTime = this.pathTimeCalculator.calc(
                getOperation(doneJ.getOperation()),
                getOperation(nextJ.getOperation()));

        this.env.process(new Path<>("", this, nextJ, pathTime));
    }

    public int run(int until) throws Exception {
        return run(until, 0);
    }

    public void printlnE10(int totalTime) {
        for (Equip<T> eq : this.equips.values()) {
            System.out.println(eq.getId());
            System.out.println(String.format("  productive: %6.2f%%", 100.0d * eq.getE10().getProductiveTime() / totalTime));
            System.out.println(String.format("     standby: %6.2f%%", 100.0d * eq.getE10().getStandbyTime() / totalTime));
        }
    }

    public void printlnE79(int totalTime) {
        for (Equip<T> eq : this.equips.values()) {
            E79 e79 = new E79(totalTime, eq.getE10());
            System.out.println(eq.getId());
            System.out.println("  availablility: " + e79.availabilityEfficiency());
            System.out.println("  operation:     " + e79.availabilityEfficiency());
        }
    }

    /**
     * Starts the simulation.
     *
     * @param until The end time.
     * @param healthCheck The cycle time used to check if the factory is idle.
     * @return The stop time.
     * @throws Exception Failed to start up.
     */
    public int run(int until, int healthCheck) throws Exception {
        if (this.equips.isEmpty()) {
            return 0;
        }
        if (this.processTimeCalculator == null) {
            throw new NullPointerException("No process time calculator. Set it up first.");
        }

        if (this.zeroTime == null) {
            this.zeroTime = new Date();
        }
        for (Op<T> op : this.operations.values()) {
            op.preload();
        }
        for (Equip<T> equip : this.equips.values()) {
            this.env.process(equip);
        }

        if (healthCheck > 0) {
            this.env.process("idle", y2 -> {
                boolean idled = false;
                while (!idled) {
                    y2.call(this.env.timeout("factory", healthCheck));
                    idled = isIdle();
                }
                this.env.stop();
            });
        }

        try {
            return this.env.run(until);
        }
        finally {
            for (Equip<T> eq : this.equips.values()) {
                eq.close();
            }
        }
    }

    public void runCrazy(final int timeForceToNext, final int checkCycle) throws Exception {
        runCrazy(timeForceToNext, checkCycle, 86400 * 60);
    }

    public void runCrazy(final int timeForceToNext, final int checkCycle, int dead) throws Exception {
        if (this.equips.isEmpty()) {
            return;
        }
        if (this.processTimeCalculator == null) {
            throw new NullPointerException("No process time calculator. Set it up first.");
        }

        if (this.zeroTime == null) {
            this.zeroTime = new Date();
        }
        for (Op<T> op : this.operations.values()) {
            op.preload();
        }
        for (Equip<T> equip : this.equips.values()) {
            equip.setEnabled(true);
            this.env.process(equip);
        }

        this.env.process("forceToNext", y2 -> {
            while (!this.products.isEmpty() && ticksNow() < dead) {
                y2.call(this.env.timeout("forceToNext", checkCycle));
                for (Op<T> op : this.operations.values()) {
                    op.forceToNext(timeForceToNext);
                }
            }
        });

        try {
            this.env.run();
        }
        finally {
            for (Equip<T> eq : this.equips.values()) {
                eq.close();
            }
        }
    }

    public void stop() {
        try {
            for (Equip<T> eq : this.equips.values()) {
                eq.close();
            }
        }
        catch (Exception ex) {

        }
        finally {
            this.env.stop();
        }
    }

    /**
     * Returns the current ticks of the simulation.
     *
     * @return The current ticks.
     */
    public int ticksNow() {
        return this.env.getNow();
    }

    /**
     * Returns the ticks of a specific time.
     *
     * @return The ticks.
     */
    public int ticks(Date time) {
        return time == null
                ? 0
                : (int) ((time.getTime() - this.zeroTime.getTime()) / this.timeType.factor);
    }

    public Date ticksTime(int ticks) {
        return new Date(this.zeroTime.getTime() + (long) ticks * this.timeType.factor);
    }

    /**
     * Returns the current time based on zero time.
     *
     * @return The current time.
     */
    public Date nowTime() {
        return new Date(this.zeroTime.getTime() + (long) ticksNow() * this.timeType.factor);
    }
}
