package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.road.helpers.JobSelector.CandidateInfo;
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
    public EquipBatch(String id, Factory<T> factory, int numCh, boolean enabled) {
        super(id, factory, enabled);
        this.chs = new ArrayList<>();
        for (int i = 1; i <= numCh; i++) {
            Channel<T> ch = new ChannelSimple<>(id + "_ch" + i, this);
            this.chs.add(ch);
        }
        this.loaded = new Vector<>();
        this.running = new Vector<>();
        this.chSelector = new ChannelSelector.Any<>();
        this.moveInNow = false;
    }

    @Override
    public void unlimit() {
    }

    @Override
    public int getLoadable() {
        return this.chs.size() - this.loaded.size() - getReservedNumber();
    }

    @Override
    public boolean isLoadable(Job<T> job) {
        if (!this.running.isEmpty()) {
            return false;
        }
        if (!job.getStrategy().acceptEquip(getId())) {
            return false;
        }
        if (isReserved(job)) {
            return true;
        }
        return this.loaded.size() + getReservedNumber() < this.chs.size();
    }

    @Override
    public List<Job<T>> getLoadedJobs() {
        return this.loaded;
    }

    @Override
    public List<Job<T>> getRunningJobs() {
        return this.running;
    }

    @Override
    public boolean isIdle() {
        return this.loaded.isEmpty() && this.running.isEmpty();
    }

    @Override
    public boolean load(Job<T> job) {
        boolean empty = this.loaded.isEmpty() && this.running.isEmpty();
        synchronized (this) {
            if (!isEnabled() || !isLoadable(job)) {
                return false;
            }
            job.setMoveInEquip(getId());
            removeReserved(job);
            this.loaded.add(job);
        }
        notifyJobs();

        int now = this.factory.ticksNow();
        if (empty) {
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
        this.factory.log(new EquipEvent(
                getId(),
                null,
                now,
                EquipEvent.MOVE_IN,
                job.getOperation(),
                job.getProductName(),
                job.getQty(),
                job.getInfo().setInt("ct", job.getPredictProcessTime())));
        JobEvent je = new JobEvent(
                job.getId(),
                job.getProductName(),
                now,
                JobEvent.MOVE_IN,
                job.getQty(),
                job.getOperation(),
                getId(),
                now - job.getDispatchedTime(),
                job.getInfo().setInt("ct", job.getPredictProcessTime()));
        je.setTimeDispatching(job.getDispatchedTime() - job.getDispatchingTime());
        this.factory.log(je);
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
                        (SimInfo) null));
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
                this.factory.log(new EquipEvent(
                        getId(),
                        null,
                        this.factory.ticksNow(),
                        EquipEvent.PULL_EMPTY,
                        null,
                        null,
                        0,
                        (SimInfo) null));
                if (this.forceToMoveIn == null) {
                    this.factory.getEnv().process(new ForceToMoveIn(60));
                }
                waitingJobs();                  // block
            }
            else {
                this.factory.getOperation(job.getOperation()).dequeue(job, getId());
                this.factory.log(new EquipEvent(
                        getId(),
                        null,
                        this.factory.ticksNow(),
                        EquipEvent.MOVE_IN,
                        job.getOperation(),
                        job.getProductName(),
                        job.getQty(),
                        job.getInfo().setInt("ct", job.getPredictProcessTime())));
                JobEvent je = new JobEvent(
                        job.getId(),
                        job.getProductName(),
                        this.factory.ticksNow(),
                        JobEvent.MOVE_IN,
                        job.getQty(),
                        job.getOperation(),
                        getId(),
                        this.factory.ticksNow() - job.getDispatchedTime(),
                        job.getInfo().setInt("ct", job.getPredictProcessTime()));
                je.setTimeDispatching(job.getDispatchedTime() - job.getDispatchingTime());
                this.factory.log(je);
                this.loaded.add(job);
                if (this.loaded.size() >= this.chs.size()) {
                    this.moveInNow = true;
                }
            }
        }
    }

    @Override
    public void processEnded(Channel<T> channel, Job<T> job, int qty) {
        synchronized (this) {
            job.processed(qty);
            moveOut(job);
        }
    }

    @Override
    public void close() {
        if (this.chNotifier != null) {
            this.chNotifier.envDown();
        }
        super.close();
    }

    private synchronized Job<T> pull() {
        if (!isEnabled()) {
            return null;
        }

        final Vector<Job<T>> jobs = new Vector<>();
        for (Op<T> op : this.operations) {
            op.getEnqueued().forEach(j -> jobs.add(j));
        }
        if (!jobs.isEmpty()) {
            CandidateInfo<T> ci = this.jobSelector.select(this, jobs);
            logDeny(ci.getIgnore());
            Job<T> job = ci.getSelected();
            return job != null && isLoadable(job) ? job : null;
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
        job.setMoveInTime(this.factory.ticksNow());
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
        int ct = now - job.getMoveInTime();
        setLastProcessedTicks(now);

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
                job.getInfo().setInt("ct", ct)));
        this.factory.log(new JobEvent(
                job.getId(),
                job.getProductName(),
                now,
                JobEvent.MOVE_OUT,
                job.getQty(),
                job.getOperation(),
                getId(),
                0,
                ct,
                job.getInfo()));

        if (job.getStrategy().getMoveOut().checkTo() && now > job.getStrategy().getMoveOut().getTo()) {
            job.updateInfo();
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    now,
                    JobEvent.QT_HOLD,
                    job.getQty(),
                    job.getOperation(),
                    getId(),
                    0,
                    job.getInfo()));
        }
        else {
            job.setMoveInEquip(getId());
            this.factory.dispatchToNext(job);
        }

        if (this.running.isEmpty()) {
            notifyJobs();
        }
        if (this.running.isEmpty() && this.loaded.isEmpty()) {
            doneProductive();
            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    now,
                    EquipEvent.IDLE_START,
                    null,
                    null,
                    new SimInfo().setInt("idled", 0)));
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
