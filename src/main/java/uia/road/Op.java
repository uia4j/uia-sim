package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.EquipSelector;
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

    private Vector<Job<T>> jobs;

    private EquipSelector<T> equipSelector;

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
     * Returns the jobs enqueued in this operation.
     * 
     * @return The jobs.
     */
    public List<Job<T>> getEnqueued() {
        return new ArrayList<>(this.jobs);
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
     * Enqueues a box in this operation.
     * 
     * @param job The job.
     * @param running If the job can be moved in automatically.
     */
    public void enqueue(Job<T> job, boolean push) {
        int now = this.factory.ticksNow();
        job.setDispatchedTime(now);
        job.updateInfo();

        // move in time control: pending
        int from = job.getStrategy().getMoveIn().getFrom();
        if (now < from) {
            // delay
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    now,
                    JobEvent.QT_PENDING,
                    this.id,
                    null,
                    0,
                    job.getInfo()));
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    job.getProductName(),
                    OpEvent.QT_PENDING,
                    this.jobs.size(),
                    job.getInfo()));
            this.factory.getEnv().process(new DelayEnqueue(job, from - now));
            return;
        }
        // move in time control: hold
        int to = job.getStrategy().getMoveIn().getTo();
        if (now > to) {
            // hold
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    now,
                    JobEvent.HOLD,
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
                now,
                JobEvent.DISPATCHED,
                this.id,
                null,
                0,
                job.getInfo()));
        if (!push || !push(job)) {
            this.jobs.add(job);
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.ENQUEUE,
                    job.getProductName(),
                    this.jobs.size(),
                    null));
        }
        else {
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.PUSH,
                    job.getProductName(),
                    this.jobs.size(),
                    null));
        }
    }

    /**
     * Dequeues the jobs from the operation.
     * 
     * @param equip The equipment.
     * @param box The box.
     * @return
     */
    public void dequeue(Job<T> job) {
        if (!this.jobs.remove(job)) {
            return;
        }

        this.factory.log(new OpEvent(
                this.id,
                this.factory.ticksNow(),
                OpEvent.PULL,
                job.getProductName(),
                this.jobs.size(),
                null));
        if (this.jobs.isEmpty()) {
            this.factory.log(new OpEvent(
                    this.id,
                    this.factory.ticksNow(),
                    OpEvent.IDLE,
                    null,
                    this.jobs.size(),
                    null));
        }
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
            if (push(job)) {
                this.jobs.remove(job);
                this.factory.log(new OpEvent(
                        this.id,
                        this.factory.ticksNow(),
                        OpEvent.PUSH,
                        job.getProductName(),
                        this.jobs.size(),
                        null));
            }
            else {
                i++;
            }
        }
        if (this.jobs.isEmpty()) {
            this.factory.log(new OpEvent(
                    this.id,
                    this.factory.ticksNow(),
                    OpEvent.IDLE,
                    null,
                    this.jobs.size(),
                    null));
        }
    }

    private synchronized boolean push(Job<T> job) {
        List<String> checked = new ArrayList<>();
        List<Equip<T>> equips = this.equips.stream()
                .filter(e -> !checked.contains(e.getId()) && !e.isLoaded())
                .collect(Collectors.toList());

        Equip<T> equip = this.equipSelector.select(job, equips);
        boolean pushed = false;
        while (equip != null) {
            checked.add(equip.getId());
            if (equip.load(job)) {
                pushed = true;
                equip = null;
            }
            else {
                equips = this.equips.stream()
                        .filter(e -> !checked.contains(e.getId()) && !e.isLoaded())
                        .collect(Collectors.toList());
                equip = this.equipSelector.select(job, equips);
            }
        }
        return pushed;
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
