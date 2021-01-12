package uia.sim.events;

import java.util.List;

import uia.sim.Env;
import uia.sim.Event;

/**
 * The abstract condition event.
 * 
 * @author Kan
 *
 */
public abstract class Condition extends Event {

	protected final List<Event> events;
	
	private int checkCount;
	
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
		
		// Immediately succeed if no events are provided.
		if(events.isEmpty()) {
			succeed(new ConditionValue());
			return;
		}
		
		// Check if the condition is met for each processed event. 
		// Attach method::check() as a callback otherwise.
        for(Event event : events) {
        	if(event.isProcessed()) {
        		check(event);
        	}
        	else {
    			event.addCallable(this::check);
        	}
		}
		
		// Register a callable which will build the value of this condition
        // after it has been triggered.
		addCallable(this::buildValue);
	}
	
	private void buildValue(Event event) {
		removeCheck();
		if(event.isOk()) {
			ConditionValue cv = new ConditionValue();
			setValue(cv);
			populateValue(cv);
		}
	}
	
	private void populateValue(ConditionValue cv) {
		for(Event event : events) {
			event.removeCallable(this::check);
			if(event instanceof Condition) {
				((Condition)event).populateValue(cv);
			}
			else if(event.isProcessed()) {
				cv.getEvents().add(event);
			}
		}
	}
	
	private void removeCheck() {
		for(Event event : events) {
			event.removeCallable(this::check);
			if(event instanceof Condition) {
				((Condition)event).removeCheck();
			}
		}
	}

	private void check(Event event) {
		if(isTriggered()) {
			return;
		}
		
		this.checkCount++;
		if(!event.isOk()) {
			// Abort if the event has failed.
			event.defused();
			// event.fail(event.getValue());
		}
		else if(evaluate(this.events, checkCount)) {
			// The condition has been met. The buildValue() callable will
            // populate the ConditionValue once this condition is processed.
			succeed(null);;
		}
	}
	
	/**
	 * Evaluates if events met the criteria.
	 * 
	 * @param events The events used to evaluate.
	 * @param count the pass count.
	 * @return Pass or not.
	 */
	protected abstract boolean evaluate(List<Event> events, int count);

}
