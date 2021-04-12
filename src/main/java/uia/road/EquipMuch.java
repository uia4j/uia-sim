package uia.road;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;

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

    private ArrayList<Channel<T>> chs;

    private Event waitingCh;

    private Event waitingLoad;

    private int loadPorts;

    private TreeMap<String, JobBox<T>> boxes;

    private TreeMap<String, JobBox<T>> preload;

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
        this.boxes = new TreeMap<>();
        this.preload = new TreeMap<>();
        this.chs = new ArrayList<>();
        for (int i = 1, c = Math.max(1, chCount); i <= c; i++) {
            Channel<T> ch = new Channel<>(id + "_ch" + i, this);
            this.chs.add(ch);
        }
    }

    @Override
    public boolean isBusy() {
        Optional<Channel<T>> opts = this.chs.stream().filter(c -> !c.isProcessing()).findAny();
        return !opts.isPresent();
    }

    @Override
    public void addPreload(Job<T> job) {
        JobBox<T> box = this.preload.get(job.getBoxId());
        if (box == null) {
            box = new JobBox<>(job.getBoxId(), job.getOperation());
            this.boxes.put(box.getId(), box);
            box.addJob(job);
        }
        this.preload.put(box.getId(), box);
    }

    @Override
    protected void run() {
        // preload
        try {
            for (JobBox<T> box : this.preload.values()) {
                try {
                    for (Job<T> job : box.getJobs()) {
                        Channel<T> ch = findChannel();
                        ch.run(job);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.preload.clear();

        // normal
        while (yield().isAlive()) {
            // check box loading
            if (this.boxes.size() >= this.loadPorts) {
                EquipEvent e1 = new EquipEvent(
                        getId(),
                        null,
                        this.factory.now(),
                        EquipEvent.BUSY,
                        null,
                        null,
                        null);
                this.factory.log(e1);
                this.waitingLoad = this.getFactory().getEnv().event("waiting");
                yield(this.waitingLoad);
                this.waitingLoad = null;
                continue;
            }

            // check if some channel is available
            // TODO: Allow to move in jobs if channels are not available?
            Channel<T> ch = findChannel();

            // find jobs
            JobBox<T> box = new JobBox<>("unknown", null);
            for (Op<T> op : this.operations) {
                box = op.dequeue(this);
                if (!box.isEmpty()) {
                    this.operations.remove(op);
                    this.operations.add(op);
                    break;
                }
            }

            // waiting new jobs
            if (box.isEmpty()) {
                waitingJobs();
                continue;
            }

            int now = this.factory.now();

            // move in
            box.setMoveInTime(now);
            this.boxes.put(box.getId(), box);
            updateStrategy(box, EquipEvent.MOVE_IN);

            this.factory.log(new EquipEvent(
                    getId(),
                    null,
                    now,
                    EquipEvent.MOVE_IN,
                    box.getOperation(),
                    box.getId(),
                    box.getInfo()));
            box.getJobs().forEach(j -> {
                j.updateInfo();
                this.factory.log(new JobEvent(
                        j.getId(),
                        j.getBoxId(),
                        now,
                        JobEvent.MOVE_IN,
                        getId(),
                        now - j.getDispatchedTime(),
                        j.getInfo()));
            });

            try {
                for (Job<T> job : box.getJobs()) {
                    ch = findChannel();
                    ch.run(job);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void processStarted(Channel<T> channel, Job<T> job) {
    }

    @Override
    public void processEnded(Channel<T> channel, Job<T> job) {
        JobBox<T> box = this.boxes.get(job.getBoxId());

        if (this.waitingCh != null) {
            this.waitingCh.succeed(channel);
            this.waitingCh = null;
        }
        if (box.isFinished()) {
            TimeStrategy ts = box.calcMoveOutStrategy();
            int delay = ts.getFrom() - this.getFactory().now();
            if (delay <= 0) {
                moveOut(box);
            }
            else {
                this.factory.getEnv().process(new MoveOut(box, delay));
            }
        }
    }

    private void moveOut(JobBox<T> box) {
        int now = this.getFactory().now();

        // move out
        box.setMoveOutTime(now);
        this.boxes.remove(box.getId());
        updateStrategy(box, EquipEvent.MOVE_OUT);
        // 
        if (this.waitingLoad != null) {
            this.waitingLoad.succeed(this);
            this.waitingLoad = null;
        }

        this.factory.log(new EquipEvent(
                getId(),
                null,
                now,
                EquipEvent.MOVE_OUT,
                box.getOperation(),
                box.getId(),
                box.getInfo()));
        box.getJobs().forEach(j -> {
            j.updateInfo();
            this.factory.log(new JobEvent(
                    j.getId(),
                    j.getBoxId(),
                    now,
                    JobEvent.MOVE_OUT,
                    getId(),
                    0,
                    j.getInfo()));
        });

        TimeStrategy ts = box.calcMoveOutStrategy();
        if (now <= ts.getTo()) {
            this.factory.dispatch(box);
        }
        else {
            box.getJobs().forEach(j -> {
                j.updateInfo();
                this.factory.log(new JobEvent(
                        j.getId(),
                        j.getBoxId(),
                        this.factory.now(),
                        JobEvent.HOLD,
                        getId(),
                        0,
                        j.getInfo()));
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Channel<T> findChannel() {
        Channel<T> ch = null;
        Optional<Channel<T>> opts = this.chs.stream().filter(c -> !c.isProcessing()).findAny();
        ch = opts.isPresent() ? opts.get() : null;
        if (ch == null) {
            this.waitingCh = this.getFactory().getEnv().event("waiting_ch");
            ch = (Channel<T>) yield(this.waitingCh);
            this.waitingCh = null;
        }
        return ch;
    }

    class MoveOut extends Processable {

        private final JobBox<T> box;

        private final int delay;

        public MoveOut(JobBox<T> box, int delay) {
            super(box.getId() + "_moveout");
            this.box = box;
            this.delay = delay;
        }

        @Override
        protected void run() {
            yield(env().timeout(this.delay));
            EquipMuch.this.moveOut(this.box);
        }

        @Override
        protected void initial() {
        }

    }
}
