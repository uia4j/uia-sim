package uia.sim.events;

import java.util.Arrays;
import java.util.List;

import uia.sim.Env;
import uia.sim.Event;

public class AllOf extends Condition {

	public AllOf(Env env, List<Event> events) {
		super(env, events);
	}

	@Override
	public Condition and(Event event) {
		this.events.add(event);
		return this;
	}
	
	@Override
	public Condition or(Event event) {
		return new AnyOf(env, Arrays.asList(this, event));
	}

	@Override
	public String toString() {
		return "AllOf(" + getId() + ")";
	}

	@Override
	protected boolean evaluate(List<Event> events, int okEvents) {
		return events.size() == okEvents;
	}
}
