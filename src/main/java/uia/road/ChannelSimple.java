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
public class ChannelSimple<T> implements Channel<T> {

    private final String id;

    private final Equip<T> equip;

    private final SimInfo info;

    private Job<T> job;

    private boolean processing;

    private int batchSize;

    private int compensationTime;

    private int runningQty;

    private int maxProcessTime;

    /**
     * The constructor.
     *
     * @param id The channel id.
     * @param equip The equipment owns the channel.
     */
    public ChannelSimple(String id, Equip<T> equip) {
        this.id = id;
        this.equip = equip;
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
                this::run);
        return true;
    }

    protected final void run(Yield2Way<Event, Object> yield) {
        final int qty = this.runningQty;

        this.job.updateInfo();
        Factory<T> factory = this.equip.getFactory();

        // current operation
        // process time
        int processTime = this.equip.getProcessTimeCalculator()
                .calc(this.equip, this.job).total;
        processTime += (this.compensationTime + this.equip.getCompensationTime());
        processTime = Math.min(Math.max(60, processTime), this.maxProcessTime);

        // 1. process start
        int now1 = factory.ticksNow();
        factory.log(new EquipEvent(
                this.equip.getId(),
                this.id,
                now1,
                EquipEvent.PROCESS_START,
                this.job.getOperation(),
                this.job.getProductName(),
                qty,
                this.job.getInfo()));
        factory.log(new JobEvent(
                this.job.getId(),
                this.job.getProductName(),
                now1,
                JobEvent.PROCESS_START,
                qty,
                this.job.getOperation(),
                this.id,
                now1 - this.job.getMoveInTime(),
                processTime,
                this.job.getInfo()));

        // 2. processing
        yield.call(factory.getEnv().timeout(
                this.job.getProductName() + "_" + this.id + "_process_end",
                processTime));

        // 3. process end
        int now2 = factory.ticksNow();
        factory.log(new EquipEvent(
                this.equip.getId(),
                this.id,
                now2,
                EquipEvent.PROCESS_END,
                this.job.getOperation(),
                this.job.getProductName(),
                qty,
                this.job.getInfo()));
        factory.log(new JobEvent(
                this.job.getId(),
                this.job.getProductName(),
                now2,
                JobEvent.PROCESS_END,
                qty,
                this.job.getOperation(),
                this.id,
                0,
                this.job.getInfo()));
        Job<T> job = this.job;
        this.job = null;
        this.processing = false;
        this.equip.processEnded(this, job, qty);
    }

    @Override
    public String toString() {
        return String.format("%s processing:%s", this.id, this.processing);
    }
}
