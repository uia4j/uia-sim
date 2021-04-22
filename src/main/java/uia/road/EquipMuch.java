package uia.road;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class EquipMuch<T> extends Equip<T> {

    private final int loadPorts;

    private final ArrayList<Channel<T>> chs;

    private final List<Job<T>> loaded;

    private final List<Job<T>> running;

    private Event waitingCh;

    private int idleStart;

    /**
     * The constructor.
     * 
     * @param id The equipment id.
     * @param factory The factory.
     * @param loadPorts The max boxes in the equipment.
     * @param chCount The channel number.
     */
    public EquipMuch(String id, Factory<T> factory, int loadPorts, int chCount) {
        super(id, factory);
        this.loadPorts = loadPorts <= 0 ? Integer.MAX_VALUE : loadPorts;
        this.chs = new ArrayList<>();
        for (int i = 1, c = Math.max(1, chCount); i <= c; i++) {
            Channel<T> ch = new Channel<>(id + "_ch" + i, this);
            this.chs.add(ch);
        }
        this.loaded = new Vector<>();
        this.running = new Vector<>();
    }

    @Override
    public boolean isLoaded() {
        return (this.loaded.size() + this.running.size()) >= this.loadPorts;
    }

    @Override
    public boolean isIdle() {
        return this.running.isEmpty();
    }

    @Override
    public boolean load(Job<T> job) {
        synchronized (this) {
            if (isLoaded()) {
                return false;
            }
            if (!job.load(getId())) {
                return false;
            }
            this.loaded.add(job);
        }
        notifyJobs();
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
                        null));
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
                waitingJobs();                  // block
            }
            else {
                this.factory.getOperation(job.getOperation()).dequeue(job);
                moveIn(job);                    // block maybe
            }
        }
    }

    @Override
    public void processEnded(Channel<T> channel, Job<T> job) {
        synchronized (this) {
            if (this.waitingCh != null) {
                this.waitingCh.succeed(channel);
                this.waitingCh = null;
            }
        }

        int delay = job.getStrategy().getMoveOut().getFrom() - this.getFactory().ticksNow();
        if (delay > 0) {
            this.factory.getEnv().process(new MoveOut(job, delay));
        }
        else {
            moveOut(job);
        }
    }

    private Job<T> pull() {
        ArrayList<Job<T>> jobs = new ArrayList<>();
        for (Op<T> op : this.operations) {
            jobs.addAll(op.getEnqueued());
        }
        return this.jobSelector.select(this, jobs);
    }

    /**
     * May be <b>blocked<b> if no workable channel.
     * 
     * @param job The job.
     */
    private void moveIn(Job<T> job) {
        int now = this.factory.ticksNow();

        if (this.running.isEmpty()) {
            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    this.factory.ticksNow(),
                    EquipEvent.IDLE_END,
                    null,
                    null,
                    new SimInfo().setInt("idled", now - this.idleStart)));
        }

        // move in
        job.setMoveInTime(now);
        updateStrategy(job, EquipEvent.MOVE_IN);
        //
        this.running.add(job);
        this.loaded.remove(job);
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

        try {
            // may be blocked
            findChannel().run(job);
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
        if (this.running.isEmpty()) {
            this.idleStart = this.factory.ticksNow();
            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    this.factory.ticksNow(),
                    EquipEvent.IDLE_START,
                    null,
                    null,
                    null));
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
                    this.factory.ticksNow(),
                    JobEvent.HOLD,
                    job.getOperation(),
                    getId(),
                    0,
                    job.getInfo()));
        }
    }

    @SuppressWarnings("unchecked")
    private Channel<T> findChannel() {
        Channel<T> ch = null;
        Optional<Channel<T>> opts = this.chs.stream().filter(c -> !c.isProcessing()).findAny();
        ch = opts.isPresent() ? opts.get() : null;
        if (ch == null) {
            this.waitingCh = this.getFactory().getEnv().event(getId() + "_waiting_ch");
            ch = (Channel<T>) yield(this.waitingCh);
            this.waitingCh = null;
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
