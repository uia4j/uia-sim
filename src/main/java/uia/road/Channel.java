package uia.road;

import uia.cor.Yield2Way;
import uia.road.events.JobEvent;
import uia.sim.Event;

/**
 * The working channel in the equipment.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Channel<T> {

    private final String id;

    private final Equip<T> equip;

    private Job<T> job;

    private boolean processing;

    /**
     * The constructor.
     *
     * @param id The channel id.
     * @param equip The equipment owns the channel.
     */
    public Channel(String id, Equip<T> equip) {
        this.id = id;
        this.equip = equip;
    }

    /**
     * Tests if the channel is working.
     *
     * @return True if the channel is working.
     */
    public boolean isProcessing() {
        return this.processing;
    }

    /**
     * Run the job in this channel.
     *
     * @param job The job.
     * @throws Exception Failed to run the job.
     */
    public void run(Job<T> job) throws Exception {
        if (this.job != null) {
            throw new Exception(String.format("%s CAN NOT move in. %s is running in the %s. time:%s",
                    job.getProductName(),
                    this.job.getProductName(),
                    this.id,
                    this.equip.getFactory().getEnv().getNow()));
        }

        this.job = job;
        this.processing = true;
        this.equip.getFactory()
                .getEnv()
                .process(job.getProductName(), this::run);
    }

    protected final void run(Yield2Way<Event, Object> yield) {
        this.job.updateInfo();
        Factory<T> factory = this.equip.getFactory();

        // current operation
        // process time
        int processTime = this.equip.getProcessTimeCalculator()
                .calc(this.equip, this.job);

        // 1. process start
        int now1 = factory.now();
        factory.log(new JobEvent(
                this.job.getProductName(),
                this.job.getBoxId(),
                now1,
                this.job.isProcessing() ? JobEvent.PROCESSING : JobEvent.PROCESS_START,
                this.id,
                now1 - this.job.getDispatchedTime(),
                this.job.getInfo()));
        this.job.setProcessing(true);
        this.job.setFinished(false);
        this.equip.processStarted(this, this.job);

        // 2. processing
        yield.call(factory.getEnv().timeout(processTime));
        this.job.setProcessing(false);
        this.job.setFinished(true);

        // 3. process end
        int now2 = factory.now();
        factory.log(new JobEvent(
                this.job.getProductName(),
                this.job.getBoxId(),
                now2,
                JobEvent.PROCESS_END,
                this.id,
                0,
                this.job.getInfo()));
        Job<T> job = this.job;
        this.job = null;
        this.processing = false;
        this.equip.processEnded(this, job);
    }
}
