package uia.sim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.Event.PriorityType;
import uia.sim.events.Process;

public class Observable<T> {

    private static final Logger logger = LogManager.getLogger(Observable.class);

    private final Env env;

    private final String id;

    private Event notifyEvent;

    public Observable(Env env, String id) {
        this.env = env;
        this.id = id;
        this.notifyEvent = new Event(this.env, this.id);
    }

    public synchronized Event ask(Process from) {
        logger.info(String.format("%4d> %s> %s observe",
                this.env.getNow(),
                this.id,
                from));
        return this.notifyEvent;
    }

    public synchronized void available(T by) {
        if (this.notifyEvent.getNumberOfCallbables() == 0) {
            return;
        }

        try {
            Event evt = this.notifyEvent;
            this.notifyEvent = new Event(env, id);
            evt.succeed(by, PriorityType.LOW);
            logger.info(String.format("%4d> %s> %s available",
                    this.env.getNow(),
                    this.id,
                    by));
        }
        catch (Exception ex) {
            logger.error(String.format("%s> available() failed: %s", this.id, ex.getMessage()), ex);
        }
    }

}
