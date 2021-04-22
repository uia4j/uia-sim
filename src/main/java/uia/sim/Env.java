package uia.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;
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

    private final String id;

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
        this("ENV");
    }

    /**
     * The constructor.
     *
     * @param id The environment id.
     */
    public Env(String id) {
        this(id, 0);
    }

    /**
     * The constructor.
     *
     * @param initialTime The initial time.
     */
    public Env(int initialTime) {
        this("env", initialTime);
    }

    /**
     * The constructor.
     *
     * @param id The environment id.
     * @param initialTime The initial time.
     */
    public Env(String id, int initialTime) {
        this.id = id;
        this.jobs = new PriorityBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(1);
        this.now = Math.max(0, initialTime);
        this.initialTime = this.now;
    }

    /**
     * Returns the id.
     *
     * @return The id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the listener.
     *
     * @return The environment listener.
     */
    public EnvListener getListener() {
        return this.listener;
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

    public int size() {
        return this.jobs.size();
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
        return new Timeout(this, "delay" + delay, delay);
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
        if (time < this.now) {
            throw new IllegalArgumentException(String.format("time(=%s) must be > the current simulation time.", time));
        }
        Event event = new Event(this, id);
        event.addCallable(e -> runnable.run());
        Job job = new Job(event, priority, time);
        this.jobs.add(job);
        logger.info(String.format("%4d> %s>  sch> %s,uid=%s at %s",
                getNow(),
                this.id,
                job.event,
                job.event.seqNo,
                job.time));
        if (DEBUG) {
            ArrayList<String> order = new ArrayList<>();
            this.jobs.forEach(j -> order.add(j.toString()));
            logger.info(String.format("%4d> %s> jobs = %s",
                    getNow(),
                    this.id,
                    order));
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
        logger.info(String.format("%4d> %s>  sch> %s,uid=%s at %s",
                getNow(),
                this.id,
                job.event,
                job.event.seqNo,
                job.time));
        if (DEBUG) {
            logger.info(String.format("%4d> %s> jobs = %s",
                    getNow(),
                    this.id,
                    this.jobs));
        }
    }

    /**
     * Runs the environment.
     *
     * @return Stop time.
     */
    public synchronized int run() {
        logger.debug("==== start ====");
        long c = 0;
        try {
            long check = 5000;
            while (!this.jobs.isEmpty()) {
                c += step();
                if (c >= check) {
                    System.out.println(String.format("run: %s steps", c));
                    check += 5000;
                }
            }
            System.out.println(String.format("run: %s steps", c));
            logger.debug("==== end ====");
        }
        catch (Throwable ex) {
            System.out.println(String.format("run: %s steps, throw %s", c, ex.getMessage()));
            logger.debug(String.format("==== end(%s) ====", ex.getMessage()));
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
        if (until < this.now) {
            throw new IllegalArgumentException(String.format("until(=%s) must be > the current simulation time.", until));
        }
        Event stopEvent = event("stop");
        stopEvent.addCallable(this::stopSim);
        schedule(stopEvent, PriorityType.URGENT, until - this.now);

        logger.info("==== start ====");
        long c = 0;
        try {
            long check = 5000;
            while (this.now < until && !this.jobs.isEmpty()) {
                c += step();
                if (c >= check) {
                    System.out.println(String.format("run: %s steps", c));
                    check += 5000;
                }
            }
            System.out.println(String.format("run: %s steps", c));
            logger.info("==== end ====");
        }
        catch (Throwable ex) {
            System.out.println(String.format("run: %s steps, throw %s", c, ex.getMessage()));
            logger.debug(String.format("==== end(%s) ====", ex.getMessage()));
        }

        while (!this.jobs.isEmpty()) {
            try {
                this.jobs.poll().event.envDown();
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return this.now;
    }

    public void stop() {
        Event stopEvent = event("stop");
        stopEvent.addCallable(this::stopSim);
        schedule(stopEvent, PriorityType.URGENT, 0);
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
            this.listener.stepDone(time, processId, event.forLog());
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
     * Checks the first job.
     * 
     * @return The first job.
     */
    public Job seeFirstJob() {
        return this.jobs.peek();
    }

    /**
     * Processes the next event.
     *
     * @throws SimEventException
     *
     */
    protected int step() throws RuntimeException {
        return stepOne();
        // return stepParallel();
    }

    private int stepOne() throws RuntimeException {
        // 1. get the first job.
        Job job = this.jobs.poll();

        // 2. update environment time
        this.now = job.time;
        logger.debug(String.format("%4d> %s> step> %s, callbacks(%s)",
                getNow(),
                this.id,
                job.event,
                job.event.getNumberOfCallbables()));

        // 3. callback the event.
        job.event.callback();

        // 4. check if event is OK.
        if (!job.event.isOk() && !job.event.isDefused()) {
            throw new SimEventException(job.event, job.event + " is NG and not defused");
        }

        return 1;
    }

    @SuppressWarnings("unused")
    private int stepParallel() throws RuntimeException {
        // 1. get jobs with the same time.
        int now = this.jobs.peek().time;
        ArrayList<Job> jobs = new ArrayList<>();
        while (!this.jobs.isEmpty() && this.jobs.peek().time == now) {
            jobs.add(this.jobs.poll());
        }
        this.now = now;
        final Vector<RuntimeException> result = new Vector<>();
        logger.info(String.format("%4d> %s> step> parallel, %s",
                getNow(),
                this.id,
                jobs));
        jobs.parallelStream().forEach(job -> {
            logger.info(String.format("%4d> %s> step> %s, callbacks(%s)",
                    getNow(),
                    this.id,
                    job.event,
                    job.event.getNumberOfCallbables()));

            // 3. callback the event.
            try {
                job.event.callback();
                // 4. check if event is OK.
                if (!job.event.isOk() && !job.event.isDefused()) {
                    throw new SimEventException(job.event, job.event + " is NG and not defused");
                }
            }
            catch (SimStopException ex) {
                result.add(0, ex);
            }
            catch (RuntimeException ex) {
                result.add(ex);
            }
        });
        if (!result.isEmpty()) {
            throw result.get(0);
        }
        return jobs.size();
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
        throw new SimStopException();
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
            return String.format("%s at %s", this.event, this.time);
        }
    }
}
