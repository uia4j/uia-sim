package uia.sim.events;

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
        // beautiful design
        return new AllOf(this.env, id, Arrays.asList(this, event));
    }

    @Override
    public Condition or(String id, Event event) {
        return new AnyOf(this.env, id, Arrays.asList(this, event));
    }

    @Override
    public String toString() {
        return "AnyOf(" + getId() + ")";
    }

    @Override
    protected boolean evaluate(List<Event> events) {
        return events.stream()
                .filter(e -> e.isProcessed())
                .findFirst().isPresent();
    }
}
