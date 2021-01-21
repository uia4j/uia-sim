package uia.sim.events;

import java.util.ArrayList;
import java.util.List;

import uia.sim.Event;

/**
 * The condition value.
 *
 * @author Kan
 *
 */
public class ConditionValue {

    private final List<Event> events;

    /**
     * The constructor.
     */
    public ConditionValue() {
        this.events = new ArrayList<>();
    }

    /**
     * Returns the events within this condition.
     *
     * @return The events.
     */
    public List<Event> getEvents() {
        return this.events;
    }

}
