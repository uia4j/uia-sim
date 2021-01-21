package uia.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.events.AllOf;
import uia.sim.events.AnyOf;
import uia.sim.events.Condition;

/**
 * The base event.
 *
 * @author Kan
 *
 */
public class Event {

    private static final Logger logger = LogManager.getLogger(Event.class);

    private static final Object PENDING = new Object();

    /**
     * The priority type.
     *
     * @author Kan
     *
     */
    public enum PriorityType {

        URGENT(0),

        HIGH(1),

        NORMAL(2),

        LOW(3);

        public final int level;

        PriorityType(int level) {
            this.level = level;
        }
    }

    protected final Env env;

    protected final String id;

    protected final int seqNo;

    private Object value;

    private boolean envDown;

    private boolean ok;

    private boolean processed;

    private boolean defused;

    private ArrayList<Consumer<Event>> callables;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param id The event id.
     */
    public Event(Env env, String id) {
        this(env, id, PENDING);
    }

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param id The event id.
     * @param value The value of the event.
     */
    public Event(Env env, String id, Object value) {
        this.env = env;
        this.id = id;
        this.seqNo = env == null ? Integer.MAX_VALUE : env.genSeq();
        this.value = value;
        this.envDown = false;
        this.ok = true;
        this.processed = false;
        this.defused = false;
        this.callables = new ArrayList<>();
    }

    /**
     * Returns the environment of the event.
     *
     * @return The environment.
     */
    public Env getEnv() {
        return this.env;
    }

    /**
     * Returns the event id.
     *
     * @return The event id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the value of the event.
     *
     * @return The value.
     */
    public Object getValue() {
        //if(this.value == PENDING) {
        //	throw new IllegalStateException("Value of event:" + this.id + " is not yet available");
        //}
        return this.value;
    }

    /**
     * Sets the value to the event.
     *
     * @param value The value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the number of callable instances.
     *
     * @return The number of callable instances.
     */
    public int getNumberOfCallbables() {
        return this.callables.size();
    }

    /**
     * Adds a callable instance which will be executed at the scheduled time.
     *
     * @param callable A callable instance.
     * @return Successful added or not.
     */
    public boolean addCallable(Consumer<Event> callable) {
        if (this.callables.contains(callable)) {
            return false;
        }
        return this.callables.add(callable);
    }

    /**
     * Removes a callable instance.
     *
     * @param callable A callable instance.
     * @return Successful removed or not.
     */
    public boolean removeCallable(Consumer<Event> callable) {
        return this.callables.remove(callable);
    }

    /**
     * AND with other event.
     *
     * @param other The other event.
     * @return A new condition event.
     */
    public Condition and(Event other) {
        return and("and", other);
    }

    /**
     * AND with other event.
     *
     * @param id The condition event id.
     * @param other The other event.
     * @return A new condition event.
     */
    public Condition and(String id, Event other) {
        return new AllOf(this.env, id, Arrays.asList(this, other));
    }

    /**
     * OR with other event.
     *
     * @param id The condition event id.
     * @param other The other event.
     * @return A new condition event.
     */
    public Condition or(Event other) {
        return or("or", other);
    }

    /**
     * OR with other event.
     *
     * @param id The condition event id.
     * @param other The other event.
     * @return A new condition event.
     */
    public Condition or(String id, Event other) {
        return new AnyOf(this.env, id, Arrays.asList(this, other));
    }

    /**
     * Tests if the environment is down or not.
     *
     * @return The if the environment is down.
     */
    public boolean isEnvDown() {
        return this.envDown;
    }

    /**
     * Notifies the environment is down.
     *
     */
    public void envDown() {
        this.ok = false;
        this.envDown = true;
        callback();
    }

    /**
     * Tests if the event has been scheduled into the environment.<br>
     *
     * Invoke one of trigger(), succeed(), fail() to schedule this event for processing by the environment.
     *
     * @return Scheduled or not.
     */
    public boolean isTriggered() {
        return this.value != PENDING;
    }

    /**
     * Tests if the event has been processed.
     *
     * @return Processed or not.
     */
    public boolean isProcessed() {
        return this.processed;
    }

    /**
     * Tests if the event is Ok or not.
     *
     * @return OK or not.
     */
    public boolean isOk() {
        return this.ok;
    }

    /**
     * Tests if failed event's exception has been defused or not.<br>
     *
     * When an event fails (i.e. calling method`fail()), the failed event's value
     * is an exception that will be re-raised when the
     * environment processes the event (i.e. in environment method::step()`).
     *
     * It is also possible for the failed event's exception to be defused by
     * setting defused property to true from an event callback.
     *
     * Doing so prevents the event's exception from being re-raised when the event is
     * processed by the environment.
     *
     * @return True when failed event's exception has been defused.
     */
    public boolean isDefused() {
        return this.defused;
    }

    /**
     * NG the event. All the callables WILL NOT be invoked.
     *
     */
    public void ng() {
        this.ok = false;
    }

    /**
     * Defuses the event.
     *
     */
    public void defused() {
        this.defused = true;
    }

    /**
     * Updates the event with the state and value of the provided event and
     * <b>schedule</b> it for processing by the environment.
    .	 *
     * @param event The specific event.
     */
    public synchronized void trigger(Event event) {
        logger.debug(String.format("%4d> %s> triggered(uid=%s) by %s", this.env.getNow(), getId(), this.seqNo, event.getId()));
        this.ok = event.isOk();
        this.value = event.value;
        // schedule
        this.env.schedule(this, PriorityType.NORMAL);
    }

    /**
     * Marks it as successful and
     * <b>schedule</b> it for processing by the environment.
     *
     * @param value The value of the event.
     */
    public synchronized void succeed(Object value) {
        succeed(value, PriorityType.NORMAL);
    }

    /**
     * Marks it as successful and
     * <b>schedule</b> it for processing by the environment.
     *
     * @param value The value of the event.
     * @param priority The priority.
     */
    public synchronized void succeed(Object value, PriorityType priority) {
        logger.debug(String.format("%4d> %s> succeed(uid=%s)", this.env.getNow(), getId(), this.seqNo));
        if (isTriggered()) {
            throw new SimEventException(this, "The event:" + this.id + " has alreday been triggered");
        }

        this.ok = true;
        this.value = value;
        // schedule
        this.env.schedule(this, priority);
    }

    /**
     * Marks the event as failed with a cause and
     * <b>schedule</b> it for processing by the environment.
     *
     * @param cause The failed cause.
     */
    public synchronized void fail(Exception cause) {
        logger.debug(String.format("%4d> %s> fail(uid=%s), %s", this.env.getNow(), getId(), this.seqNo, cause.getMessage()));
        if (isTriggered()) {
            throw new SimEventException(this, "The event:" + this.id + " has alreday been triggered");
        }

        this.ok = false;
        this.value = cause;
        // schedule
        this.env.schedule(this, PriorityType.NORMAL);
    }

    /**
     * Executes all instances of callable.<br>
     * The event will be marked as 'processed' at the same time.
     *
     */
    public synchronized void callback() {
        logger.info(String.format("%4d> %s> -- call(uid=%s) --", this.env.getNow(), this, this.seqNo));
        this.processed = true;
        try {
            while (!this.callables.isEmpty()) {
                this.callables.remove(0).accept(this);
            }
        }
        finally {
            logger.debug(String.format("%4d> %s> == done(uid=%s) ==", this.env.getNow(), this, this.seqNo));
            this.callables.clear();
        }
    }

    /**
     * Creates a readonly version event.
     *
     * @return A readonly event.
     */
    public final Event forLog() {
        Event event = new Event(null, toString());
        event.ok = this.ok;
        event.processed = this.processed;
        event.defused = this.defused;
        event.value = this.value;
        event.envDown = this.envDown;
        return event;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * Returns detail information of this event.
     *
     * @return The information.
     */
    public String toFullString() {
        return String.format("%s(uid=%s,ok=%s,triggered=%s,proccessed=%s)",
                getId(),
                this.seqNo,
                this.isOk(),
                this.isTriggered(),
                this.isProcessed());
    }
}
