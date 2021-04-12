package uia.sim;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.Event.PriorityType;;

public class Notifier<T> {

    private static final Logger logger = LogManager.getLogger(Notifier.class);

    private final Env env;

    private final String id;

    private final Vector<Event> waitingEvents;

    public Notifier(Env env, String id) {
        this.env = env;
        this.id = id;
        this.waitingEvents = new Vector<>();
    }

    public synchronized Event waiting(String listener) {
        Event e = new Event(this.env, listener);
        this.waitingEvents.add(e);
        return e;
    }

    public synchronized Event waiting(String listener, int timeout) {
        Event e = new Event(this.env, listener);
        this.env.process("notifier_timeout", y2 -> {
            y2.call(this.env.timeout(listener, timeout));
            if (this.waitingEvents.remove(e) && !e.isTriggered()) {
                e.succeed(null);
            }
        });
        this.waitingEvents.add(e);
        return e;
    }

    public synchronized void available(T by) {
        logger.info(String.format("%4d> %s> %s available",
                this.env.getNow(),
                this.id,
                by));
        while (!this.waitingEvents.isEmpty()) {
            Event e = this.waitingEvents.remove(0);
            if (!e.isTriggered()) {
                e.succeed(by, PriorityType.LOW);
            }
        }
    }
}
