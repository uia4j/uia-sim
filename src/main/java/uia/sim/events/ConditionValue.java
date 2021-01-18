package uia.sim.events;

import java.util.ArrayList;
import java.util.List;

import uia.sim.Event;

public class ConditionValue {

    private List<Event> events;

    public ConditionValue() {
        this.events = new ArrayList<>();
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

}
