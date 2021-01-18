package uia.sim.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uia.sim.Env;
import uia.sim.Event;

/**
 * AnyOf condition event.
 * 
 * @author Kan
 *
 */
public class AnyOf extends Condition {

    /**
     * The constructor.
     * 
     * @param env The environment.
     * @param id The event id.
     * @param events The events used to check pass or not.
     */
    public AnyOf(Env env, String id, List<Event> events) {
        super(env, id, events);
    }

    @Override
    public Condition and(String id, Event event) {
        return new AllOf(env, id, Arrays.asList(this, event));
    }

    @Override
    public Condition or(String id, Event event) {
        this.events.add(event);
        return new AnyOf(this.env, id, new ArrayList<>(this.events));
    }

    @Override
    public String toString() {
        return "AnyOf(" + getId() + ")";
    }

    @Override
    protected boolean evaluate(List<Event> events, int okEvents) {
        return events.size() == 0 || okEvents > 0;
    }
}
