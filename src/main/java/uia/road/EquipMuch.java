package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

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
public class EquipMuch<T> extends Equip<T> {

    private final int loadPorts;

    private final ArrayList<Channel<T>> chs;

    private final List<Job<T>> loaded;

    private final List<Job<T>> running;

    private Event chNotifier;

    private ChannelSelector<T> chSelector;

    /**
     * The constructor.
     *
     * @param id The equipment id.
     * @param factory The factory.
     * @param loadPorts The max boxes in the equipment.
     * @param chCount The channel number.
     */
    public EquipMuch(String id, Factory<T> factory, int loadPorts, int chCount, boolean enabled) {
        super(id, factory, enabled);
        this.loadPorts = loadPorts <= 0 ? Integer.MAX_VALUE : loadPorts;
        this.chs = new ArrayList<>();
        for (int i = 1, c = Math.max(1, chCount); i <= c; i++) {
            Channel<T> ch = new Channel<>(id + "_ch" + i, this);
            this.chs.add(ch);
        }
        this.loaded = new Vector<>();
        this.running = new Vector<>();
        this.chSelector = new ChannelSelector.Any<>();
    }

    public List<Channel<T>> getChannels() {
        return this.chs;
    }

    @Override
    public int getLoadable() {
        return this.loadPorts - this.loaded.size() - this.running.size() - getReservedNumber();
    }

    @Override
    public boolean isLoadable(Job<T> job) {
        int loaded = this.loaded.size() + this.running.size();
        if (loaded >= this.loadPorts) {
            return false;
        }
        if (!job.getStrategy().acceptEquip(getId())) {
            return false;
        }
        if (isReserved(job)) {
            return true;
        }
        return loaded + getReservedNumber() < this.loadPorts;
    }

    @Override
    public boolean isIdle() {
        return this.running.isEmpty();
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
                job.getInfo()));
        this.factory.log(new JobEvent(
                job.getId(),
                job.getProductName(),
                now,
                JobEvent.MOVE_IN,
                job.getQty(),
                job.getOperation(),
                getId(),
                now - job.getDispatchedTime(),
                job.getInfo()));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void run() {
        while (yield().isAlive()) {
            // check if full loaded
            if (this.running.size() >= this.loadPorts) {
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

            // move in from load ports.
            if (!this.loaded.isEmpty()) {
                Object[] objs = this.loaded.toArray();
                for (Object obj : objs) {
                    moveIn((Job<T>) obj);       // block maybe
                }
                this.loaded.clear();
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
                        job.getInfo()));
                this.factory.log(new JobEvent(
                        job.getId(),
                        job.getProductName(),
                        this.factory.ticksNow(),
                        JobEvent.MOVE_IN,
                        job.getQty(),
                        job.getOperation(),
                        getId(),
                        this.factory.ticksNow() - job.getDispatchedTime(),
                        job.getInfo()));
                moveIn(job);                    // block maybe
            }
        }
    }

    @Override
    public void processEnded(Channel<T> channel, Job<T> job) {
        synchronized (this) {
            if (this.chNotifier != null) {
                this.chNotifier.succeed(channel);
                this.chNotifier = null;
            }

            job.processed(channel.getBatchSize() <= 0 ? job.getQty() : channel.getBatchSize());
            if (!job.isFinished()) {
                return;
            }
        }

        int delay = job.getStrategy().getMoveOut().getFrom() - this.getFactory().ticksNow();
        if (delay > 0) {
            this.factory.getEnv().process(new MoveOut(job, delay));
            new ThreadPoolExecutor(delay, delay, delay, null, null);
        }
        else {
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
            // may be blocked
            while (job.getProcessingQty() < job.getQty()) {
                Channel<T> ch = findChannel();
                ch.run(job);
            }
        }
        catch (Exception ex) {

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

        notifyJobs();

        if (now <= job.getStrategy().getMoveOut().getTo()) {
            this.factory.dispatchToNext(job);
        }
        else {
            job.updateInfo();
            this.factory.log(new JobEvent(
                    job.getId(),
                    job.getProductName(),
                    now,
                    JobEvent.HOLD,
                    job.getQty(),
                    job.getOperation(),
                    getId(),
                    0,
                    job.getInfo()));
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
            EquipMuch.this.moveOut(this.job);
        }

        @Override
        protected void initial() {
        }

    }
}
