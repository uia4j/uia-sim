package uia.sim.events;

import uia.sim.Event;
import uia.sim.SimEventException;

/**
 * Immediately schedules an Interruption event with the given cause to be thrown into the process.<br>
 *
 * This event is automatically triggered when it is created.
 *
 * @author Kan
 *
 */
public class Interruption extends Event {

    private final Process process;

    /**
     * <b>Schedules</b> a interruption event into environment.
     *
     * @param process The process should be interrupted.
     * @param cause The cause.
     * @return The interrupt event.
     */
    public static Interruption schedule(Process process, Exception cause) {
        return schedule(process, new SimEventException(process, cause));
    }

    /**
     * <b>Schedules</b> a interruption event into environment.
     *
     * @param process The process should be interrupted.
     * @param cause The cause.
     * @return The interrupt event.
     */
    public static Interruption schedule(Process process, String cause) {
        if (process.getEnv().getActiveProcess() == process) {
            throw new SimEventException(process, "The process is not allowed to interrupt itself.");
        }
        if (process.isTriggered()) {
            throw new SimEventException(process, "The process has terminated and cannot be interrupted.");
        }
        return new Interruption(process, new SimEventException(process, cause));
    }

    private Interruption(Process process, Exception cause) {
        super(process.getEnv(), "Interruption", cause);
        this.process = process;
        addCallable(this::interrupt);
        ng();
        defused();

        // schedule a event to interrupt the process.
        this.env.schedule(this, PriorityType.URGENT);
    }

    private void interrupt(Event event) {
        if (this.process.isTriggered()) {
            return;
        }

        Event target = this.process.getTarget();
        this.process.unbind(target);
        this.process.resume(this);
    }

    @Override
    public String toString() {
        return "INT(" + this.process.getId() + ")";
    }

}
