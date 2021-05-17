package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.sim.Event;
import uia.sim.Processable;

/**
 * The equipment with multiple channels.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class EquipBatch<T> extends Equip<T> {

    private final ArrayList<Channel<T>> chs;

    private final List<Job<T>> loaded;

    private final List<Job<T>> running;

    private Event chNotifier;

    private ChannelSelector<T> chSelector;

    private Event forceToMoveIn;

    private boolean moveInNow;

    /**
     * The constructor.
     * 
     * @param id The equipment id.
     * @param factory The factory.
     * @param numCh The number of channels.
     */
    public EquipBatch(String id, Factory<T> factory, int numCh) {
        super(id, factory);
        this.chs = new ArrayList<>();
        for (int i = 1; i <= numCh; i++) {
            Channel<T> ch = new Channel<>(id + "_ch" + i, this);
            this.chs.add(ch);
        }
        this.loaded = new Vector<>();
        this.running = new Vector<>();
        this.chSelector = new ChannelSelector.Any<>();
        this.moveInNow = false;
    }

    @Override
    public boolean isLoadable(Job<T> job) {
        if (!this.running.isEmpty()) {
            return false;
        }
        if (isReserved(job)) {
            return true;
        }
        return this.loaded.size() + getReservedNumber() < this.chs.size();
    }

    @Override
    public boolean isIdle() {
        return this.running.isEmpty();
    }

    @Override
    public boolean load(Job<T> job) {
        synchronized (this) {
            if (!isLoadable(job)) {
                return false;
            }
            if (!job.load(getId())) {
                return false;
            }
            removeReserved(job);
            this.loaded.add(job);
        }
        notifyJobs();

        int now = this.factory.ticksNow();
        this.factory.log(new EquipEvent(
                getId(),
                null,
                now,
                EquipEvent.MOVE_IN,
                job.getOperation(),
                job.getProductName(),
                null));
        this.factory.log(new JobEvent(
                job.getId(),
                job.getProductName(),
                now,
                JobEvent.MOVE_IN,
                job.getOperation(),
                getId(),
                now - job.getDispatchedTime(),
                job.getInfo()));
        return true;
    }

    @Override
    protected void run() {
        while (yield().isAlive()) {
            if (this.running.size() > 0) {
                this.factory.log(new EquipEvent(
                        getId(),
                        null,
                        this.factory.ticksNow(),
                        EquipEvent.BUSY,
                        null,
                        null,
                        null));
                waitingJobs();                  // block
                continue;
            }

            if (this.loaded.size() >= this.chs.size() || this.moveInNow) {
                this.moveInNow = false;
                int batchSize = Math.min(this.chs.size(), this.loaded.size());
                for (int b = 0; b < batchSize; b++) {
                    Job<T> job = this.loaded.remove(0);
                    moveIn(job);
                }
                continue;
            }

            Job<T> job = pull();
            if (job == null) {
                if (this.forceToMoveIn == null) {
                    this.factory.getEnv().process(new ForceToMoveIn(60));
                }
                waitingJobs();                  // block
            }
            else {
                this.factory.getOperation(job.getOperation()).dequeue(job);
                this.factory.log(new EquipEvent(
                        getId(),
                        null,
                        this.factory.ticksNow(),
                        EquipEvent.MOVE_IN,
                        job.getOperation(),
                        job.getProductName(),
                        null));
                this.factory.log(new JobEvent(
                        job.getId(),
                        job.getProductName(),
                        this.factory.ticksNow(),
                        JobEvent.MOVE_IN,
                        job.getOperation(),
                        getId(),
                        this.factory.ticksNow() - job.getDispatchedTime(),
                        job.getInfo()));
                this.loaded.add(job);
                if (this.loaded.size() >= this.chs.size()) {
                    this.moveInNow = true;
                }
            }
        }
    }

    @Override
    public void processEnded(Channel<T> channel, Job<T> job) {
        synchronized (this) {
            job.processed(job.getQty());
            moveOut(job);
        }
    }

    private Job<T> pull() {
        ArrayList<Job<T>> jobs = new ArrayList<>();
        for (Op<T> op : this.operations) {
            jobs.addAll(op.getEnqueued());
        }
        if (!jobs.isEmpty()) {
            Job<T> job = this.jobSelector.select(this, jobs);
            return isLoadable(job) ? job : null;
        }
        else {
            return null;
        }
    }

    /**
     * May be <b>blocked<b>.
     * 
     * @param job The job.
     */
    private void moveIn(Job<T> job) {
        int now = this.factory.ticksNow();

        if (this.running.isEmpty()) {
            int time = doneStandby();
            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    now,
                    EquipEvent.IDLE_END,
                    null,
                    null,
                    new SimInfo().setInt("idled", time)));
        }

        job.setMoveInTime(now);
        updateStrategy(job, EquipEvent.MOVE_IN);
        //
        this.running.add(job);
        this.loaded.remove(job);

        try {
            Channel<T> ch = findChannel();
            ch.run(job);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void moveOut(Job<T> job) {
        int now = this.getFactory().ticksNow();

        // move out
        job.setMoveOutTime(now);
        updateStrategy(job, EquipEvent.MOVE_OUT);

        this.running.remove(job);
        this.factory.log(new EquipEvent(
                getId(),
                null,
                now,
                EquipEvent.MOVE_OUT,
                job.getOperation(),
                job.getProductName(),
                null));
        this.factory.log(new JobEvent(
                job.getId(),
                job.getProductName(),
                now,
                JobEvent.MOVE_OUT,
                job.getOperation(),
                getId(),
                0,
                job.getInfo()));

        if (now <= job.getStrategy().getMoveOut().getTo()) {
            this.factory.dispatchToNext(job);
        }
        else {
            job.updateInfo();
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    this.factory.ticksNow(),
                    JobEvent.HOLD,
                    job.getOperation(),
                    getId(),
                    0,
                    job.getInfo()));
        }

        if (this.running.isEmpty()) {
            notifyJobs();
        }
        if (this.running.isEmpty() && this.loaded.isEmpty()) {
            doneProductive();
            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    this.factory.ticksNow(),
                    EquipEvent.IDLE_START,
                    null,
                    null,
                    null));
        }
    }

    /**
     * May be blocked.
     * @return
     */
    @SuppressWarnings("unchecked")
    private Channel<T> findChannel() {
        Channel<T> ch = this.chSelector.select(this.chs);
        if (ch == null) {
            this.chNotifier = this.getFactory().getEnv().event(getId() + "_waiting_ch");
            ch = (Channel<T>) yield(this.chNotifier);
            this.chNotifier = null;
        }
        return ch;
    }

    class MoveOut extends Processable {

        private final Job<T> job;

        private final int delay;

        public MoveOut(Job<T> job, int delay) {
            super(job.getProductName() + "_moveout");
            this.job = job;
            this.delay = delay;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.job.getProductName() + "_moveout_delay", this.delay));
            EquipBatch.this.moveOut(this.job);
        }

        @Override
        protected void initial() {
        }

    }

    class ForceToMoveIn extends Processable {

        private final int delay;

        public ForceToMoveIn(int delay) {
            super("_forceMoveIn");
            this.delay = delay;
        }

        @Override
        protected void run() {
            try {
                EquipBatch.this.forceToMoveIn = env().timeout(getId(), this.delay);
                yield(EquipBatch.this.forceToMoveIn);
            }
            catch (Exception ex) {

            }
            EquipBatch.this.moveInNow = true;
            EquipBatch.this.forceToMoveIn = null;
            EquipBatch.this.notifyJobs();
        }

        @Override
        protected void initial() {
        }

    }
}
