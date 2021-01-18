package uia.sim.events;

import uia.sim.Event;

/**
 * Immediately schedules an Initialize event to startup the process.<br>
 * 
 * This event is automatically triggered when it is created.
 *
 * @author Kan
 *
 */
public final class Initialize extends Event {

    private final Process process;

    protected Initialize(Process process) {
        super(process.getEnv(), "Initialize", null);
        this.process = process;
        this.process.bind(this);
        env.schedule(this, PriorityType.URGENT);
    }

    @Override
    public String toString() {
        return "Init(" + this.process.getId() + ")";
    }
}
