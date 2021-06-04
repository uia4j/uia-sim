package uia.road;

import uia.road.events.JobEvent;
import uia.sim.Processable;

/**
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Path<T> extends Processable {

    private final Factory<T> factory;

    private final Job<T> job;

    private final int time;

    protected Path(String id, Factory<T> factory, Job<T> job, int time) {
        super(id);
        this.factory = factory;
        this.job = job;
        this.time = time;
    }

    @Override
    protected void run() {
        int now = this.factory.ticksNow();
        this.job.updateInfo();
        this.factory.log(new JobEvent(
                this.job.getId(),
                this.job.getProductName(),
                now,
                JobEvent.DISPATCHING,
                this.job.getQty(),
                this.job.getOperation(),
                null,
                0,
                this.job.getInfo()));
        try {
            this.job.setDispatchingTime(now);
            yield(env().timeout(this.time));
            this.factory.dispatch(this.job);
        }
        catch (Exception ex) {

        }
    }

    @Override
    protected void initial() {
    }
}
