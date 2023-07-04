package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.EquipSelector;
import uia.road.helpers.EquipSelector.CandidateInfo;
import uia.sim.Processable;

/**
 * The operation in the factory.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Op<T> {

    private final String id;

    private final Factory<T> factory;

    private final Vector<Equip<T>> equips;

    private final SimInfo info;

    private final Vector<Job<T>> jobs;

    private EquipSelector<T> equipSelector;

    private int index;

    /**
     * The constructor.
     *
     * @param id The operation id.
     * @param factory The factory.
     */
    public Op(String id, Factory<T> factory) {
        this.id = id;
        this.factory = factory;
        this.equips = new Vector<>();
        this.info = new SimInfo();
        this.jobs = new Vector<>();
        this.equipSelector = new EquipSelector.Any<>();
    }

    /**
     * Returns the operation id.
     *
     * @return The operation id.
     */
    public String getId() {
        return this.id;
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
     * Returns all equipments the operation serves.
     *
     * @return
     */
    public List<Equip<T>> getEquips() {
        return new ArrayList<>(this.equips);
    }

    /**
     * Returns information for simulating.
     *
     * @return The information for simulating.
     */
    public SimInfo getInfo() {
        return this.info;
    }

    /**
     * Returns the jobs enqueued in this operation.
     *
     * @return The jobs.
     */
    public List<Job<T>> getEnqueued() {
        return new ArrayList<>(this.jobs);

    }

    /**
     * Returns the equipment selector.
     *
     * @return The equipment selector.
     */
    public EquipSelector<T> getEquipSelector() {
        return this.equipSelector;
    }

    /**
     * Sets the equipment selector.
     *
     * @param equipSelector The equipment selector.
     */
    public void setEquipSelector(EquipSelector<T> equipSelector) {
        this.equipSelector = equipSelector;
    }

    /**
     * Serves the equipment.
     *
     * @param eq The equipment.
     */
    public void serve(Equip<T> eq) {
        if (eq == null) {
            return;
        }
        if (!this.equips.contains(eq)) {
            this.equips.add(eq);
            eq.serve(this);
        }
    }

    /**
     * Forces all jobs to move to next operation.
     *
     * @param waitAfter Time after.
     * @return False if no jobs in the queue.
     */
    public synchronized boolean forceToNext(int waitAfter) {
        if (this.jobs.isEmpty()) {
            return false;
        }
        int now = this.factory.ticksNow();
        List<Job<T>> nexts = new ArrayList<>();
        for (Job<T> job : this.jobs) {
            if (job.getDispatchedTime() + waitAfter <= now) {
                nexts.add(job);
                this.factory.log(new JobEvent(
                        job.getId(),
                        job.getProductName(),
                        now,
                        JobEvent.FORCE_JUMP,
                        job.getQty(),
                        job.getOperation(),
                        null,
                        0,
                        job.getInfo()));
                this.factory.dispatchToNext(job);
            }
        }
        for (Job<T> job : nexts) {
            this.jobs.remove(job);
        }
        nexts.clear();
        return true;
    }

    /**
     * Enqueues a job in this operation.
     *
     * @param job The job.
     * @param forceToPush Force to push to the equipment.
     */
    public void enqueue(Job<T> job, boolean forceToPush) {
        int now = this.factory.ticksNow();
        if (job.getDispatchedTime() >= 0) {
            job.setDispatchedTime(now);
        }
        job.setIndex(this.index++);
        job.updateInfo();

        // move in time control: pending
        int from = job.getStrategy().getMoveIn().getFrom();
        if (now < from) {
            // delay
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.QT_PENDING,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.QT_PENDING,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    null,
                    job.getInfo()));
            this.factory.getEnv().process(new DelayEnqueue(job, from - now));
            return;
        }
        // move in time control: hold
        int to = job.getStrategy().getMoveIn().getTo();
        if (job.getStrategy().getMoveIn().checkTo() && now > to) {
            // hold
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.HOLD,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            return;
        }
        Equip<T> eq = forceToPush ? push(job) : null;
        if (eq == null) {
            this.jobs.add(job);
            // job
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.DISPATCHED,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            // operation
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.ENQUEUE,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    null,
                    job.getInfo()));
        }
        else if (eq instanceof Equip.Die) {
            // job
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.DISPATCHED,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()).deny("DIE", "DIE"));
            // operation
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.ENQUEUE,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    "DIE",
                    job.getInfo()).deny("DIE", "DIE"));
        }
        else {
            // job
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.DISPATCHED,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            // operation
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.PUSH,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    eq.getId(),
                    job.getInfo()));
        }
    }

    /**
     * Enqueues a job in this operation.
     *
     * @param job The job.
     * @param forceToPush Force to push to the equipment.
     */
    public void enqueueNoDelay(Job<T> job, boolean forceToPush) {
        int now = this.factory.ticksNow();
        if (job.getDispatchedTime() >= 0) {
            job.setDispatchedTime(now);
        }
        job.setIndex(this.index++);
        job.updateInfo();

        // move in time control: hold
        int to = job.getStrategy().getMoveIn().getTo();
        if (job.getStrategy().getMoveIn().checkTo() && now > to) {
            // hold
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.HOLD,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            return;
        }
        // dispatched
        this.factory.log(new JobEvent(
                job.getId(),
                job.getProductName(),
                job.getDispatchedTime(),
                JobEvent.DISPATCHED,
                job.getQty(),
                this.id,
                null,
                0,
                job.getInfo()));

        Equip<T> eq = forceToPush ? push(job) : null;
        if (eq == null) {
            this.jobs.add(job);
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.ENQUEUE,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    null,
                    job.getInfo()));
        }
        else if (eq instanceof Equip.Die) {
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.ENQUEUE,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    null,
                    job.getInfo()).deny("DIE", "ZERO loadable equipments"));
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    job.getDispatchedTime(),
                    JobEvent.DISPATCHED,
                    job.getQty(),
                    this.id,
                    null,
                    0,
                    job.getInfo()).deny("DIE", "ZERO loadable equipments"));
        }
        else {
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.PUSH,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    eq.getId(),
                    job.getInfo()));
        }
    }

    /**
     * Dequeues the jobs from the operation.
     *
     * @param equip The equipment.
     * @param box The box.
     * @return
     */
    public void dequeue(Job<T> job, String by) {
        if (!this.jobs.remove(job)) {
            this.factory.log(new OpEvent(
                    this.id,
                    this.factory.ticksNow(),
                    OpEvent.PULL,
                    job.getProductName(),
                    this.jobs.size(),
                    this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                    by,
                    job.getInfo()));
            return;
        }

        this.factory.log(new OpEvent(
                this.id,
                this.factory.ticksNow(),
                OpEvent.PULL,
                job.getProductName(),
                this.jobs.size(),
                this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                by,
                job.getInfo()));
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Op) {
            return this.id.equals(((Op<?>) o).getId());
        }
        else {
            return false;
        }
    }

    protected synchronized void preload() {
        int i = 0;
        while (i < this.jobs.size()) {
            Job<T> job = this.jobs.get(i);
            Equip<T> eq = push(job);
            if (eq != null && !(eq instanceof Equip.Die)) {
                this.jobs.remove(job);
                this.factory.log(new OpEvent(
                        this.id,
                        this.factory.ticksNow(),
                        OpEvent.PUSH,
                        job.getProductName(),
                        this.jobs.size(),
                        this.jobs.stream().mapToInt(j -> j.getQty()).sum(),
                        eq.getId(),
                        job.getInfo()));
            }
            else {
                i++;
            }
        }
    }

    private synchronized Equip<T> push(Job<T> job) {
        int now = this.factory.ticksNow();
        int qty = this.jobs.stream().mapToInt(j -> j.getQty()).sum();
        try {
            List<Equip<T>> equips = this.equips.stream()
                    .filter(e -> e.isEnabled())
                    .collect(Collectors.toList());
            CandidateInfo<T> ci = this.equipSelector.select(job, equips);
            for (Equip<T> equip : ci.getIgnore()) {
                this.factory.log(new OpEvent(
                        getId(),
                        now,
                        OpEvent.DENY,
                        job.getProductName(),
                        this.jobs.size(),
                        qty,
                        equip.getId(),
                        new SimInfo().setString("ignore", equip.getPriorityInfo())));
            }
            for (Equip<T> equip : ci.getPassed()) {
                if (equip.load(job)) {
                    return equip;
                }
            }

            return ci.getIgnore().size() != this.equips.size()
                    ? null
                    : new Equip.Die<>(this.factory);
        }
        catch (Exception ex) {
            this.factory.log(new OpEvent(
                    getId(),
                    now,
                    OpEvent.ERROR,
                    job.getProductName(),
                    this.jobs.size(),
                    qty,
                    null,
                    new SimInfo().setString("ignore", ex.getMessage())));
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * A Delay enqueue process.
     *
     * @author Kan
     *
     */
    public class DelayEnqueue extends Processable {

        private final Job<T> job;

        private final int delay;

        /**
         * The constructor.
         *
         * @param box The box.
         * @param delay The delay time.
         */
        public DelayEnqueue(Job<T> job, int delay) {
            super(job.getProductName() + "_delay_enqueue");
            this.job = job;
            this.delay = delay;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.delay));
            Op.this.enqueue(this.job, true);
        }

        @Override
        protected void initial() {
        }

    }
}
