package uia.sim;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Yield2Way;
import uia.cor.Yieldable2Way;
import uia.sim.Event.PriorityType;
import uia.sim.events.AllOf;
import uia.sim.events.AnyOf;
import uia.sim.events.Process;
import uia.sim.events.Timeout;

/**
 * The simulation environment.<br>
 * 
 * <p>
 * The simplest example:
 * </p>
 * <pre>{@code
 * final Env env = new Env();
 * env.process("Hello", y -> {
 *     y.call(env.timeout("10"));
 *     System.out.println(env.getNow() + ", Hello DESimJava");
 * }); 
 * env.run();
 * }</pre>
 * Above example will output: 
 * <pre>
 * 10, Hello DESimJava
 * </pre>
 *
 * @author Kan
 *
 */
public class Env {

    public static boolean DEBUG = true;

    private static final Logger logger = LogManager.getLogger(Env.class);

    private int seqNo;

    protected int initialTime;

    protected PriorityBlockingQueue<Job> jobs;

    private int now;

    private Process activeProcess;

    private EnvListener listener;

    private ExecutorService executor;

    static {
        UUID.randomUUID();
    }

    /**
     * The constructor.
     */
    public Env() {
        this.jobs = new PriorityBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(1);
    }

    /**
     * The constructor.
     * 
     * @param initialTime The initial time.
     */
    public Env(int initialTime) {
        this.jobs = new PriorityBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(1);
        this.now = Math.max(0, initialTime);
        this.initialTime = now;
    }

    /**
     * Returns the listener.
     * 
     * @return The environment listener.
     */
    public EnvListener getListener() {
        return listener;
    }

    /**
     * Sets the listener.
     * 
     * @param listener The environment listener.
     */
    public void setListener(EnvListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the current time of the environment..
     * 
     * @return The time.
     */
    public int getNow() {
        return this.now;
    }

    /**
     * Returns the active process event.
     * 
     * @return The active process event.
     */
    public Process getActiveProcess() {
        return this.activeProcess;
    }

    /**
     * Sets the active process event.
     * 
     * @param activeProcess The active process event.
     */
    public void setActiveProcess(Process activeProcess) {
        this.activeProcess = activeProcess;
    }

    /**
     * Creates a new process event.<br>
     * 
     * <p>
     * The process will pass a <b>yield</b> object to the <b>taskRunner</b>. 
     * When <b>taskRunner</b> invokes yeild.call(<b>anotherEvent</b>), the yield object will notify the process 
     * to attach <u>method:resume</u> to the callables of the <b>anotherEvent</b>.
     * </p> 
     * 
     * <p>
     * When the environment steps to the <b>anotherEvent</b>, the <u>method:resume</u> of callable will be invoked.
     * </p>
     * 
     * <p>
     * Note: <b>anotherEvent</b> must be scheduled into the environment, otherwise callables will never be invoked.
     * 
     * @param id The process id.
     * @param taskRunner A runner of the process tasks.
     * @return A new process event. 
     */
    public Process process(String id, Consumer<Yield2Way<Event, Object>> taskRunner) {
        return new Process(this, id, taskRunner);
    }

    /**
     * Creates a new process event.<br>
     * 
     * @param id The process id.
     * @param taskRunner A runner of the process tasks.
     * @return A new process event. 
     */
    public Process process(String id, Yieldable2Way<Event, Object> taskRunner) {
        return new Process(this, id, taskRunner);
    }

    /**
     * Creates a new process event.<br>
     * 
     * @param processable A runner of the process tasks.
     * @return A new process event. 
     */
    public Process process(Processable processable) {
        return processable.bind(this);
    }

    /**
     * <b>Schedules</b> a new timeout event for processing by this environment.
     * 
     * @param delay The delay time.
     * @return A new scheduled timeout event.
     */
    public Timeout timeout(int delay) {
        return new Timeout(this, delay);
    }

    /**
     * <b>Schedules</b> a new timeout event for processing by this environment.
     * 
     * @param delay The delay time.
     * @param value The value of the event.
     * @return A new scheduled timeout event.
     */
    public Timeout timeout(int delay, Object value) {
        return new Timeout(this, "delay" + delay, delay, value);
    }

    /**
     * <b>Schedules</b> a new timeout event for processing by this environment.
     * 
     * @param id The event id.
     * @param delay The delay time.
     * @return A new scheduled timeout event.
     */
    public Timeout timeout(String id, int delay) {
        return new Timeout(this, id, delay);
    }

    /**
     * <b>Schedules</b> a new timeout event for processing by this environment.
     * 
     * @param id The event id.
     * @param delay The delay time.
     * @param value The value of the event.
     * @return A new scheduled timeout event.
     */
    public Timeout timeout(String id, int delay, Object value) {
        return new Timeout(this, id, delay, value);
    }

    /**
     * Creates a new event instance.
     * 
     * @param id The event id.
     * @return A new event.
     */
    public Event event(String id) {
        return new Event(this, id);
    }

    /**
     * Creates a new event instance.
     * 
     * @param id The event id.
     * @param events The events.
     * @return A new condition event.
     */
    public Event andOf(String id, Event... events) {
        return new AnyOf(this, id, Arrays.asList(events));
    }

    /**
     * Creates a new event instance.
     * 
     * @param id The event id.
     * @param events The events.
     * @return A new condition event.
     */
    public Event allOf(String id, Event... events) {
        return new AllOf(this, id, Arrays.asList(events));
    }

    /**
     * Adds a schedule in the environment.
     * 
     * @param id The event id.
     * @param time The time.
     * @param priority The priority.
     * @param runnable The job.
     */
    public void schedule(String id, int time, Event.PriorityType priority, Runnable runnable) {
        if (time < now) {
            throw new IllegalArgumentException(String.format("time(=%s) must be > the current simulation time.", time));
        }
        Event event = new Event(this, id);
        event.addCallable(e -> runnable.run());
        Job job = new Job(event, priority, time);
        this.jobs.add(job);
        logger.info(String.format("%4d> %s> schedule(%s) at %s", getNow(), job.event, job.event.seqNo, job.time));
        if (DEBUG) {
            logger.info(String.format("%4d> jobs = %s", getNow(), jobs));
        }
    }

    /**
     * Adds a schedule in the environment.
     * 
     * @param event The event.
     * @param priority The priority.
     */
    public void schedule(Event event, Event.PriorityType priority) {
        schedule(event, priority, 0);
    }

    /**
     * Adds a schedule in the environment.
     * 
     * @param event The event.
     * @param priority The priority.
     * @param delay The delay time.
     */
    public void schedule(Event event, Event.PriorityType priority, int delay) {
        Job job = new Job(event, priority, this.now + delay);
        this.jobs.add(job);
        logger.info(String.format("%4d> %s> schedule(%s) at %s", getNow(), job.event, job.event.seqNo, job.time));
        if (DEBUG) {
            logger.info(String.format("%4d> jobs = %s", getNow(), jobs));
        }
    }

    /**
     * Runs the environment.
     * 
     * @return Stop time.
     */
    public synchronized int run() {
        logger.debug("==== start ====");
        try {
            while (!this.jobs.isEmpty()) {
                step();
            }
            logger.debug("==== end ====");
        }
        catch (Throwable ex) {
            logger.debug("==== end with exception ====");
        }

        while (!this.jobs.isEmpty()) {
            this.jobs.poll().event.envDown();
        }
        return this.now;
    }

    /**
     * Executes events until the given criterion until is met.
     * 
     * @param until The end time.
     * @return Stop time.
     */
    public synchronized int run(final int until) {
        logger.debug("==== start ====");
        if (until < this.now) {
            throw new IllegalArgumentException(String.format("until(=%s) must be > the current simulation time.", until));
        }
        Event stopEvent = event("stop");
        stopEvent.addCallable(this::stopSim);
        schedule(stopEvent, PriorityType.URGENT, until - this.now);
        try {
            while (this.now < until && !jobs.isEmpty()) {
                step();
            }
            logger.debug("==== end ====");
        }
        catch (Throwable ex) {
            logger.debug("==== end with exception ====");
        }

        while (!this.jobs.isEmpty()) {
            this.jobs.poll().event.envDown();
            // this.jobs.remove(0).event.envDown();
        }
        return this.now;
    }

    /**
     * Raises a 'Process Done' information to the listener.
     * 
     * @param time The time.
     * @param processId The process id.
     * @param event The event.
     */
    public void raiseProcessDone(int time, String processId, Event event) {
        if (this.listener != null) {
            this.executor.submit(() -> this.listener.stepDone(time, processId, event.forLog()));
        }
    }

    /**
     * Raises a 'Process Failed' information to the listener.
     * 
     * @param time The time.
     * @param processId The process id.
     * @param event The event.
     */
    public void raiseProcessFailed(int time, String processId, Event event) {
        if (this.listener != null) {
            this.executor.submit(() -> this.listener.stepFailed(time, processId, event.forLog()));
        }
    }

    /**
     * Processes the next event.
     * 
     * @throws SimException 
     * 
     */
    protected void step() throws SimException {
        // 1. get the first job.
        Job job = this.jobs.poll();

        // 2. update environment time
        this.now = job.time;
        logger.debug(String.format("%4d> ENV> STEP> %4s> %s, callbacks(%s)", getNow(), this.now, job.event, job.event.getNumberOfCallbables()));

        // 3. callback the event.
        job.event.callback();

        // 4. check if event is OK.
        if (!job.event.isOk() && !job.event.isDefused()) {
            throw new SimException(job.event, job.event + " is NG and not defused");
        }
    }

    /**
     * Generates next sequence number.
     * 
     * @return The sequence number.
     */
    protected int genSeq() {
        synchronized (this.jobs) {
            return ++this.seqNo;
        }
    }

    private void stopSim(Event event) {
        throw new StopSimException("stop the simulation");
    }

    /**
     * The job for scheduling.
     * 
     * @author Kan
     *
     */
    class Job implements Comparable<Job> {

        /**
         * The event.
         */
        public final Event event;

        /**
         * The priority.
         */
        public final Event.PriorityType priority;

        /**
         * The time to be scheduled in the environment.
         */
        public final int time;

        /**
         * Constructor.
         * 
         * @param event The event.
         * @param priority The priority.
         * @param time The time to be scheduled in the environment.
         */
        Job(Event event, Event.PriorityType priority, int time) {
            this.event = event;
            this.priority = priority;
            this.time = time;
        }

        @Override
        public int compareTo(Job c2) {
            int c = this.time - c2.time;
            if (c != 0) {
                return c;
            }
            c = this.priority.level - c2.priority.level;
            if (c != 0) {
                return c;
            }

            return this.event.seqNo - c2.event.seqNo;
        }

        @Override
        public String toString() {
            return String.format("%s at %s", event, time);
        }
    }
}
