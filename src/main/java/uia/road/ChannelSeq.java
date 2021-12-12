package uia.road;

import uia.cor.Yield2Way;
import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.sim.Event;

/**
 * The working channel in the equipment.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class ChannelSeq<T> implements Channel<T> {

    private final String id;

    private final Equip<T> equip;

    private final SimInfo info;

    private Job<T> job;

    private boolean processing;

    private int batchSize;

    private int compensationTime;

    private int runningQty;

    private int maxProcessTime;

    private int firstStepTime;

    /**
     * The constructor.
     *
     * @param id The channel id.
     * @param equip The equipment owns the channel.
     */
    public ChannelSeq(String id, Equip<T> equip, int firstStepTime) {
        this.id = id;
        this.equip = equip;
        this.firstStepTime = firstStepTime;
        this.info = new SimInfo();
        this.batchSize = 0;
        this.maxProcessTime = 43200;
    }

    @Override
    public int getMaxProcessTime() {
        return this.maxProcessTime;
    }

    @Override
    public void setMaxProcessTime(int maxProcessTime) {
        this.maxProcessTime = Math.max(60, maxProcessTime);
    }

    @Override
    public int getBatchSize() {
        return this.batchSize;
    }

    @Override
    public void setBatchSize(int batchSize) {
        this.batchSize = Math.max(0, batchSize);
    }

    @Override
    public int getCompensationTime() {
        return this.compensationTime;
    }

    @Override
    public void setCompensationTime(int compensationTime) {
        this.compensationTime = Math.max(0, compensationTime);
    }

    @Override
    public SimInfo getInfo() {
        return this.info;
    }

    /**
     * Tests if the channel is working.
     *
     * @return True if the channel is working.
     */
    @Override
    public boolean isProcessing() {
        return this.processing;
    }

    /**
     * Run the job in this channel.
     *
     * @param job The job.
     * @throws Exception Failed to run the job.
     */
    @Override
    public synchronized boolean run(Job<T> job) throws Exception {
        if (this.job != null) {
            return false;
        }

        this.processing = true;
        this.job = job;
        this.runningQty = this.job.processing(this.batchSize <= 0 ? job.getQty() : this.batchSize);
        this.equip.getFactory().getEnv().process(
                job.getProductName() + "_" + this.id + "_process",
                this::runJob);
        return true;
    }

    protected final void runFirstStep(Yield2Way<Event, Object> yield) {
        Factory<T> factory = this.equip.getFactory();
        yield.call(factory.getEnv().timeout(
                this.job.getProductName() + "_" + this.id + "_first_step",
                this.firstStepTime));
        this.processing = false;
    }

    protected final void runJob(Yield2Way<Event, Object> yield) {
        final int qty = this.runningQty;
        final Job<T> _job = this.job;

        _job.updateInfo();
        Factory<T> factory = this.equip.getFactory();

        // current operation
        // process time
        int processTime = this.equip.getProcessTimeCalculator()
                .calc(this.equip, _job).total;
        processTime += (this.compensationTime + this.equip.getCompensationTime());
        processTime = Math.min(Math.max(60, processTime), this.maxProcessTime);

        // 1. process start
        int now1 = factory.ticksNow();
        factory.log(new EquipEvent(
                this.equip.getId(),
                this.id,
                now1,
                EquipEvent.PROCESS_START,
                _job.getOperation(),
                _job.getProductName(),
                qty,
                _job.getInfo()));
        factory.log(new JobEvent(
                _job.getId(),
                _job.getProductName(),
                now1,
                JobEvent.PROCESS_START,
                qty,
                _job.getOperation(),
                this.id,
                now1 - _job.getMoveInTime(),
                processTime,
                _job.getInfo()));

        // 2. processing
        yield.call(factory.getEnv().timeout(
                _job.getProductName() + "_" + this.id + "_process_end",
                processTime,
                _job));

        // 3. process end
        int now2 = factory.ticksNow();
        factory.log(new EquipEvent(
                this.equip.getId(),
                this.id,
                now2,
                EquipEvent.PROCESS_END,
                _job.getOperation(),
                _job.getProductName(),
                qty,
                _job.getInfo()));
        factory.log(new JobEvent(
                _job.getId(),
                _job.getProductName(),
                now2,
                JobEvent.PROCESS_END,
                qty,
                _job.getOperation(),
                this.id,
                0,
                this.job.getInfo()));

        this.job = null;
        this.processing = false;
        this.equip.processEnded(this, _job, qty);
    }

    @Override
    public String toString() {
        return String.format("%s processing:%s", this.id, this.processing);
    }
}
