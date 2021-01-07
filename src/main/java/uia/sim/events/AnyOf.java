package uia.sim.events;

import java.util.Arrays;
import java.util.List;

import uia.sim.Env;
import uia.sim.Event;

public class AnyOf extends Condition {

	public AnyOf(Env env, List<Event> events) {
		super(env, events);
	}
	
	@Override
	public Condition and(Event event) {
		return new AllOf(env, Arrays.asList(this, event));
	}
	
	@Override
	public Condition or(Event event) {
		this.events.add(event);
		return this;
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
