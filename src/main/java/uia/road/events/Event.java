package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

public class Event implements Comparable<Event> {

    protected final int time;

    protected final String event;

    protected Map<String, Object> info;

    public Event(int time, String event) {
        this.time = time;
        this.event = event;
    }

    public Event(int time, String event, SimInfo info) {
        this.time = time;
        this.event = event;
        this.info = info == null ? null : info.toMap();
    }

    public int getTime() {
        return this.time;
    }

    public String getEvent() {
        return this.event;
    }

    public Map<String, Object> getInfo() {
        return this.info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    @Override
    public int compareTo(Event e) {
        return this.time - e.getTime();
    }
}
