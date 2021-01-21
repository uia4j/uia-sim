package uia.sim.events;

import java.util.List;
import java.util.function.Consumer;

import uia.sim.Env;
import uia.sim.Event;
import uia.sim.SimEventException;

/**
 * The abstract condition event.
 *
 * @author Kan
 *
 */
public abstract class Condition extends Event {

    private final List<Event> events;

    private final Consumer<Event> checkCallable;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param id The event id.
     * @param events The events used to check pass or not.
     */
    protected Condition(Env env, String id, List<Event> events) {
        super(env, id);
        this.events = events;
        this.checkCallable = new Consumer<Event>() {

            @Override
            public void accept(Event event) {
                Condition.this.check(event);
            }
        };

        if (this.events.isEmpty()) {
            // Immediately succeed if no events are provided.
            succeed(new ConditionValue());
        }
        else {
            // Register a callable which will build the value of this condition
            // after it has been processed.
            addCallable(this::buildValue);
            for (Event event : this.events) {
                if (event.isProcessed()) {
                    // Check if the condition is pass immediately.
                    check(event);
                }
                else {
                    // Attach checkCallback to the event.
                    // When the event is processing, it will notify this condition to check again.
                    event.addCallable(this.checkCallable);
                }
            }
        }
    }

    /**
     * NG this condition will transmit the status to the nest conditions.
     *
     */
    @Override
    public void ng() {
        super.ng();
        for (Event e : this.events) {
            if (e instanceof Condition) {
                e.ng();
            }
        }
    }

    private void buildValue(Event event) {
        removeCheck();
        if (event.isOk()) {
            ConditionValue cv = new ConditionValue();
            setValue(cv);
            populateValue(cv);
        }
    }

    private void populateValue(ConditionValue cv) {
        for (Event event : this.events) {
            event.removeCallable(this.checkCallable);
            if (event instanceof Condition) {
                ((Condition) event).populateValue(cv);
            }
            else if (event.isProcessed()) {
                cv.getEvents().add(event);
            }
        }
    }

    private void removeCheck() {
        for (Event event : this.events) {
            event.removeCallable(this.checkCallable);
            if (event instanceof Condition) {
                ((Condition) event).removeCheck();
            }
        }
    }

    private void check(Event event) {
        // ignore if this condition is triggered or NG.
        if (isTriggered() || !isOk()) {
            return;
        }

        if (evaluate(this.events)) {
            succeed(null);
        }
        else {
            boolean allChceked = !this.events.stream()
                    .filter(e -> !e.isProcessed())
                    .findFirst()
                    .isPresent();
            if (allChceked) {
                fail(new SimEventException(this, "condition failed"));
            }
        }

    }

    /**
     * Evaluates if events met the criteria.
     *
     * @param events The events used to evaluate.
     * @param count the pass count.
     * @return Pass or not.
     */
    protected abstract boolean evaluate(List<Event> events);

}
