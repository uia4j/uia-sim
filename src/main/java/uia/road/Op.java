package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.JobSelector.SelectResult;
import uia.sim.Notifier;
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

    private ArrayList<Notifier<Op<T>>> notifiers;

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
        this.notifiers = new ArrayList<>();
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
     * @return
     */
    public List<Equip<T>> getEquips() {
        return this.equips;
    }

    /**
     * Serves the equipment.
     * 
     * @param eq The equipment.
     */
    public void serve(Equip<T> eq) {
        if (!this.equips.contains(eq)) {
            this.equips.add(eq);
            eq.serve(this);
        }
    }

    /**
     * Links the operation. The operation will notify waiting equipments when new jobs are available.
     * 
     * @param notifier The notifier.
     */
    public void link(Notifier<Op<T>> notifier) {
        this.notifiers.add(notifier);
    }

    /**
     * Enqueues a box in this operation.
     * 
     * @param box The box.
     */
    public synchronized void enqueue(JobBox<T> box) {
        int now = this.factory.now();
        box.setDispatchedTime(now);

        TimeStrategy ts = box.calcMoveInStrategy();
        if (now < ts.getFrom()) {
            // delay
            box.getJobs().forEach(j -> {
                j.updateInfo();
                this.factory.log(new JobEvent(
                        j.getId(),
                        j.getBoxId(),
                        now,
                        JobEvent.QT_PENDING,
                        this.id,
                        0,
                        j.getInfo()));
            });
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.QT_PENDING,
                    box.getId(),
                    this.jobs.size(),
                    box.getInfo()));
            this.factory.getEnv().process(new DelayEnqueue(box, ts.getFrom() - now));
            return;
        }
        else if (now > ts.getTo()) {
            // hold
            box.getJobs().forEach(j -> {
                j.updateInfo();
                this.factory.log(new JobEvent(
                        j.getId(),
                        j.getBoxId(),
                        now,
                        JobEvent.HOLD,
                        this.id,
                        0,
                        j.getInfo()));
            });
            this.factory.log(new OpEvent(
                    this.id,
                    now,
                    OpEvent.HOLD,
                    box.getId(),
                    this.jobs.size(),
                    box.getInfo()));
            return;
        }

        box.getJobs().forEach(j -> {
            j.updateInfo();
            //
            this.jobs.add(j);
            this.factory.log(new JobEvent(
                    j.getId(),
                    j.getBoxId(),
                    now,
                    JobEvent.DISPATCHED,
                    this.id,
                    0,
                    j.getInfo()));
        });
        this.factory.log(new OpEvent(
                this.id,
                now,
                OpEvent.ENQUEUE,
                box.getId(),
                this.jobs.size(),
                box.getInfo()));

        // notify idled equipment.
        Random r = new Random();
        while (!this.notifiers.isEmpty()) {
            this.notifiers.remove(r.nextInt(this.notifiers.size())).available(this);
        }
    }

    /**
     * Dequeues a box from this operation for a specific equipment.
     * 
     * @param equip The requesting equipment.
     * @return The box dequeued.
     */
    public synchronized JobBox<T> dequeue(Equip<T> equip) {
        if (this.jobs.isEmpty()) {
            return new JobBox<>("uknonwn", this.id);
        }

        SelectResult<T> selected = equip.getJobSelector()
                .select(equip, this.jobs);
        if (selected.isEmpty()) {
            return new JobBox<>("uknonwn", this.id);
        }

        SimInfo boxInfo = new SimInfo();
        boxInfo.setString("equip", equip.getId());
        selected.getJobs().forEach(j -> {
            this.jobs.remove(j);
            boxInfo.addString("jobs", j.getId());
        });
        OpEvent e = new OpEvent(
                this.id,
                this.factory.now(),
                OpEvent.DEQUEUE,
                selected.getGroupId(),
                this.jobs.size(),
                boxInfo);
        this.factory.log(e);

        JobBox<T> box = new JobBox<>(selected.getGroupId(), this.id, selected.getJobs());
        box.setInfo(boxInfo);
        return box;
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

    /**
     * A Delay enqueue process.
     * 
     * @author Kan
     *
     */
    public class DelayEnqueue extends Processable {

        private final JobBox<T> box;

        private final int delay;

        /**
         * The constructor.
         * 
         * @param box The box.
         * @param delay The delay time.
         */
        public DelayEnqueue(JobBox<T> box, int delay) {
            super(box.getId() + "_delay_enqueue");
            this.box = box;
            this.delay = delay;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.delay));
            Op.this.enqueue(this.box);
        }

        @Override
        protected void initial() {
        }

    }
}
