package uia.sim.events;

import uia.sim.Env;
import uia.sim.Event;

/**
 * The event gets triggered after a *delay* has passed.<br>
 *
 * This event is automatically triggered when it is created.
 *
 * @author Kan
 *
 */
public class Timeout extends Event {

    /**
     * The constructor <b>schedule</b> itself for processing by the environment automatically.
     *
     * @param env The environment.
     * @param delay The delay time.
     */
    public Timeout(Env env, int delay) {
        this(env, "delay" + delay, delay, null);
    }

    /**
     * The constructor <b>schedule</b> itself for processing by the environment automatically.
     *
     * @param env The environment.
     * @param id The event id.
     * @param delay The delay time.
     */
    public Timeout(Env env, String id, int delay) {
        this(env, id, delay, null);
    }

    /**
     * The constructor <b>schedule</b> itself for processing by the environment automatically.
     *
     * @param env The environment.
     * @param id The event id.
     * @param delay The delay time.
     * @param value The value of the event.
     */
    public Timeout(Env env, String id, int delay, Object value) {
        super(env, id, value);
        env.schedule(this, PriorityType.NORMAL, delay);
    }

    @Override
    public String toString() {
        return String.format("Timeout(%s)", this.id);
    }
}
