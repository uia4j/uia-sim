package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.EquipStrategy;
import uia.road.helpers.PathTimeCalculator;
import uia.road.helpers.ProcessTimeCalculator;
import uia.sim.Env;

/**
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Factory<T> {

    private final Env env;

    private final TreeMap<String, Op<T>> operations;

    private final TreeMap<String, Equip<T>> equips;

    private final SimReport report;

    private ProcessTimeCalculator<T> processTimeCalculator;

    private EquipStrategy<T> equipStrategy;

    private PathTimeCalculator<T> pathTimeCalculator;

    public Factory() {
        this(0);
    }

    public Factory(int defaultPathTime) {
        this.env = new Env();
        this.operations = new TreeMap<>();
        this.equips = new TreeMap<>();
        this.report = new SimReport();
        this.pathTimeCalculator = new PathTimeCalculator.Simple<T>(defaultPathTime);
    }

    public Env getEnv() {
        return this.env;
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
                    .filter(o -> o.getEnqueued() > 0)
                    .findAny()
                    .isPresent();
        }
        return idle;
    }

    /**
     * Returns the report.
     * 
     * @return The report.
     */
    public SimReport getReport() {
        return this.report;
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

    /**
     * Logs a an operation event.
     * 
     * @param e An operation event.
     */
    public void log(OpEvent e) {
        this.report.log(e);
    }

    /**
     * Logs a equipment event.
     * 
     * @param e An equipment event.
     */
    public void log(EquipEvent e) {
        this.report.log(e);
    }

    /**
     * Logs a job event.
     * 
     * @param e A job event.
     */
    public void log(JobEvent e) {
        this.report.log(e);
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

    /**
     * Adds an operation in the factory.
     * 
     * @param op The operation.
     * @return True if the operation is added into this factory.
     */
    public boolean addOperation(Op<T> op) {
        if (this.operations.containsKey(op.getId())) {
            return false;
        }
        this.operations.put(op.getId(), op);
        return true;
    }

    /**
     * Creates an operation and add into this factory.
     * 
     * @param id The operation id.
     * @return The operation.
     */
    public Op<T> createOperation(String id) {
        Op<T> op = this.operations.get(id);
        if (op == null) {
            op = new Op<>(id, this);
            addOperation(op);
        }
        return op;
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
     * Adds an equipment in the factory.
     * 
     * @param equip The equipment.
     * @return True if the equipment is added into this factory.
     */
    public boolean addEquip(Equip<T> equip) {
        if (this.equips.containsKey(equip.getId())) {
            return false;
        }
        this.equips.put(equip.getId(), equip);
        return true;
    }

    /**
     * Creates an equipment and add into this factory.
     * 
     * @param id The equipment id.
     * @param loadPorts Number of load port.
     * @param chCount Number of the channel.
     * @return The equipment.
     */
    public Equip<T> createEquip(String id, int loadPorts, int chCount) {
        Equip<T> eq = this.equips.get(id);
        if (eq == null) {
            eq = new EquipMuch<>(id, this, loadPorts, chCount);
            addEquip(eq);
        }
        return eq;
    }

    /**
     * Dispatch the box to next operation.
     * 
     * @param box The box.
     */
    public void dispatch(JobBox<T> box) {
        Op<T> from = this.getOperation(box.getOperation());

        TreeMap<String, List<Job<T>>> nextBoxes = new TreeMap<>();
        for (Job<T> doneJ : box.getJobs()) {
            Job<T> nextJ = doneJ.getNext();
            if (nextJ == null) {
                this.log(new JobEvent(
                        doneJ.getProductName(),
                        doneJ.getBoxId(),
                        now(),
                        JobEvent.DONE,
                        null,
                        0,
                        doneJ.getInfo()));
                continue;
            }
            List<Job<T>> nextBox = nextBoxes.get(nextJ.getOperation());
            if (nextBox == null) {
                nextBox = new ArrayList<>();
                nextBoxes.put(nextJ.getOperation(), nextBox);
            }
            nextBox.add(nextJ);
        }

        SimInfo info = new SimInfo();
        int i = 1;
        for (Map.Entry<String, List<Job<T>>> e : nextBoxes.entrySet()) {
            // split
            String boxId = nextBoxes.size() == 1
                    ? box.getId()
                    : box.getId() + "." + (i++);

            JobBox<T> nextBox = new JobBox<>(boxId, e.getKey(), e.getValue());
            int pathTime = this.pathTimeCalculator.calc(
                    from,
                    getOperation(nextBox.getOperation()));
            this.env.process(new Path<T>(
                    nextBox.getId() + "_dispatch_to_" + nextBox.getOperation(),
                    this,
                    nextBox,
                    pathTime));
            info.addString("split", boxId);
        }

        if (nextBoxes.size() > 1) {
            log(new OpEvent(
                    from.getId(),
                    now(),
                    OpEvent.SPLIT,
                    box.getId(),
                    from.getEnqueued(),
                    info));
        }
    }

    public boolean prepare(Job<T> job) {
        JobBox<T> box = new JobBox<T>(job.getBoxId(), job.getOperation(), job);
        box.getInfo().addString("jobs", job.getProductName());
        return prepare(box);
    }

    /**
     * Prepares a box at specific operation.
     * @param opId The operation id.
     * @param box The box.
     * @return True if the box is placed at specific operation.
     */
    public boolean prepare(JobBox<T> box) {
        Op<T> op = this.operations.get(box.getOperation());
        if (op == null) {
            return false;
        }
        op.enqueue(box);
        return true;
    }

    /**
     * Prepares a job into a specific equipment.
     * @param eqId The equipment id.
     * @param job The job.
     * @return True if the job is loaded into the equipment.
     */
    public boolean prepare(Job<T> job, String eqId) {
        Equip<T> eq = this.equips.get(eqId);
        if (eq == null) {
            return false;
        }
        eq.addPreload(job);
        return true;
    }

    public int run(int until) throws Exception {
        return run(until, 1800);
    }

    /**
     * Starts the simulation.
     * 
     * @param until The end time.
     * @param cycleTime The cycle time used to check if the factory is idle.
     * @return The stop time.
     * @throws Exception Failed to start up.
     */
    public int run(int until, int cycleTime) throws Exception {
        if (this.equips.isEmpty()) {
            return 0;
        }
        if (this.processTimeCalculator == null) {
            throw new NullPointerException("No process time calculator. Set it up first.");
        }

        for (Equip<T> equip : this.equips.values()) {
            this.env.process(equip);
        }

        this.env.process("idle", y2 -> {
            boolean idled = false;
            while (!idled) {
                y2.call(this.env.timeout("factory", cycleTime));
                idled = isIdle();
            }
            this.env.stop();
        });

        return this.env.run(until);
    }

    /**
     * Returns the current time of the simulation.
     * 
     * @return The current time.
     */
    public int now() {
        return this.env.getNow();
    }
}
