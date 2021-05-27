package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

public abstract class Event implements Comparable<Event> {

    protected final int time;

    protected final String event;

    protected Map<String, Object> info;

    protected Event(int time, String event, SimInfo info) {
        this.time = time;
        this.event = event;
        this.info = info == null ? null : info.toMap();
    }

    protected Event(int time, String event, Map<String, Object> info) {
        this.time = time;
        this.event = event;
        this.info = info;
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

    @Override
    public String toString() {
        return String.format("%8d %-15s", getTime(), getEvent());
    }
}
