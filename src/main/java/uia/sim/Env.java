package uia.sim;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Yield2Way;
import uia.sim.Event.PriorityType;
import uia.sim.events.Process;
import uia.sim.events.Timeout;

/**
 * The simulation environment.
 * 
 * @author Kan
 *
 */
public class Env {
	
    private static final Logger logger = LogManager.getLogger(Env.class);
	
	protected int initialTime;
	
	protected PriorityBlockingQueue<Job> jobs;

	private int now;
	
	private Process activeProcess;
	
	/**
	 * The constructor.
	 */
	public Env() {
		this.jobs = new PriorityBlockingQueue<>();
	}

	/**
	 * The constructor.
	 * 
	 * @param initialTime The initial time.
	 */
	public Env(int initialTime) {
		this.jobs = new PriorityBlockingQueue<>();
		this.now = Math.max(0, initialTime);
		this.initialTime = now;
	}

	/**
	 * Returns the current time of the environment..
	 * 
	 * @return The time.
	 */
	public int getNow() {
		return this.now;
	}
	
	public Process getActiveProcess() {
		return this.activeProcess;
	}

	public void setActiveProcess(Process activeProcess) {
		this.activeProcess = activeProcess;
	}

	/**
	 * Creates a new process event.<br>
	 * 
	 * <p>
	 * The process will pass a <b>yield</b> object to the <b>taskRunner</b>. 
	 * When <b>taskRunner</b> invokes yeild.call(<b>anotherEvent</b>), yield will notify the process 
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
	 * @param taskRunner A runner of the process tasks.
	 * @return A new process event. 
	 */
	public Process process(String name, Consumer<Yield2Way<Event, Object>> taskRunner) {
		return new Process(this, name, taskRunner);
	}

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
		return new Timeout(this, delay, value);
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
		// jobs.sort(this::sortJobs);
		logger.debug(String.format("ENV>  SCH> %4s> %s, queue(%s)", job.time, job.event, jobs.size()));
	}
	
	/**
	 * Runs the environment.
	 * 
	 * @return Stop time.
	 */
	public synchronized int run() {
		logger.debug("==== start ====");
		try {
			while(!this.jobs.isEmpty()) {
				step();
			}
			logger.debug("==== end ====");
		}
		catch(Throwable ex) {
			logger.debug("==== end with exception ====");
		}
		
		while(!this.jobs.isEmpty()) {
			this.jobs.poll().event.envDown();
			// this.jobs.remove(0).event.envDown();
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
		if(until < this.now) {
			throw new IllegalArgumentException(String.format("until(=%s) must be > the current simulation time.", until));
		}
		Event stopEvent = event("stop");
		stopEvent.addCallable(this::stopSim);
		schedule(stopEvent, PriorityType.URGENT, until - this.now);
		try {
			while(this.now < until && !jobs.isEmpty()) {
				step();
			}
			logger.debug("==== end ====");
		}
		catch(Throwable ex) {
			logger.debug("==== end with exception ====");
		}

		while(!this.jobs.isEmpty()) {
			this.jobs.poll().event.envDown();
			// this.jobs.remove(0).event.envDown();
		}
		return this.now;
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
		// Job job = this.jobs.remove(0);
		logger.debug(String.format("ENV> STEP> dequeue(%s)", this.jobs.size()));
		// 2. update environment time
		this.now = job.time;
		logger.debug(String.format("ENV> STEP> %4s> %s, callbacks(%s)", this.now, job.event, job.event.getNumberOfCallbables()));
		// 3. callback the event.
		job.event.callback();
		logger.debug(String.format("ENV> STEP> %4s> %s, callbacks done", this.now, job.event));
		// 4. check if event is OK.
		if(!job.event.isOk() && !job.event.isDefused()) {
			throw new SimException(job.event, job.event + " is NG and not defused");
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
		
		public final Event event;
		
		public final Event.PriorityType priority;
		
		public final int time;

		/**
		 * Constructor.
		 * 
		 * @param event The event.
		 * @param priority The priority.
		 * @param time The time to be executed.
		 */
		Job(Event event, Event.PriorityType priority, int time) {
			this.event = event;
			this.priority = priority;
			this.time = time;
		}

		@Override
		public int compareTo(Job c2) {
			int c = this.time - c2.time;
			return c == 0 ? this.priority.level - c2.priority.level : c;
		}
		
		@Override
		public String toString() {
			return String.format("%s, time=%s", event, time);
		}
	}
}
