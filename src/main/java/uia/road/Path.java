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

    private final JobBox<T> box;

    private final int time;

    protected Path(String id, Factory<T> factory, JobBox<T> box, int time) {
        super(id);
        this.factory = factory;
        this.box = box;
        this.time = time;
    }

    @Override
    protected void run() {
        int now = this.factory.now();
        this.box.getJobs().forEach(j -> {
            j.updateInfo();
            this.factory.log(new JobEvent(
                    j.getId(),
                    j.getBoxId(),
                    now,
                    JobEvent.DISPATCHING,
                    this.box.getOperation(),
                    0,
                    j.getInfo()));
        });

        yield(env().timeout(this.time));
        this.factory.preload(this.box);
    }

    @Override
    protected void initial() {
    }
}
