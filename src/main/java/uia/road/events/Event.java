package uia.road.events;

import java.util.Map;

import uia.road.SimInfo;

/**
 * The event. Used to keep information within simulating.
 *
 * @author Kan
 *
 */
public abstract class Event implements Comparable<Event> {

    /**
     * Event: error.
     */
    public static final String ERROR = "ERROR";

    /**
     * The time ticks.
     */
    protected final int time;

    /**
     * The event name.
     */
    protected final String event;

    /**
     * The event information.
     */
    protected Map<String, Object> info;

    private String denyCode;

    private String denyInfo;

    /**
     * The constructor.
     *
     * @param time The time ticks.
     * @param event The event name.
     * @param info The information.
     */
    protected Event(int time, String event, SimInfo info) {
        this.time = time;
        this.event = event;
        this.info = info == null ? null : info.toMap();
    }

    /**
     * The constructor.
     *
     * @param time The time ticks.
     * @param event The event name.
     * @param info The information.
     */
    protected Event(int time, String event, Map<String, Object> info) {
        this.time = time;
        this.event = event;
        this.info = info;
    }

    /**
     * Returns the time ticks.
     *
     * @return The time ticks.
     */
    public int getTime() {
        return this.time;
    }

    /**
     * Returns event name.
     *
     * @return Event name.
     */
    public String getEvent() {
        return this.event;
    }

    /**
     * Returns event information.
     *
     * @return Event information.
     */
    public Map<String, Object> getInfo() {
        return this.info;
    }

    /**
     * Sets event information.
     *
     * @return Event information.
     */
    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    /**
     * Returns deny code.
     *
     * @return Deny code.
     */
    public String getDenyCode() {
        return this.denyCode;
    }

    /**
     * Sets deny code.
     *
     * @return Deny code.
     */
    public void setDenyCode(String denyCode) {
        this.denyCode = denyCode;
    }

    /**
     * Returns deny information.
     *
     * @return Deny information.
     */
    public String getDenyInfo() {
        return this.denyInfo;
    }

    /**
     * Sets deny information.
     *
     * @return Deny information.
     */
    public void setDenyInfo(String denyInfo) {
        this.denyInfo = denyInfo;
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
