package uia.sim;

import java.util.Vector;
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

	private int now;
	
	private Vector<Job> jobs;
	
	private Process activeProcess;
	
	/**
	 * The constructor.
	 */
	public Env() {
		this.jobs = new Vector<>();
	}

	/**
	 * The constructor.
	 * 
	 * @param initialTime The initial time.
	 */
	public Env(int initialTime) {
		this.jobs = new Vector<>();
		this.now = Math.max(0, initialTime);
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
	 * Creates a new process event.
	 * 
	 * @param taskRunner A runner of the process tasks.
	 * @return A new process event. 
	 */
	public Process process(String name, Consumer<Yield2Way<Event, Object>> taskRunner) {
		return new Process(this, name, taskRunner);
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
		int a = jobs.size();
		this.jobs.add(job);
		int b = jobs.size();
		jobs.sort(this::sortJobs);
		logger.debug(String.format("ENV> SCH> %s, queue(%s->%s)", job, a, b));
	}
	
	/**
	 * Returns the time next scheduled event executes.
	 * 
	 * @return The time.
	 */
	public long peek() {
		return this.jobs.isEmpty() ? -1 : this.jobs.get(0).time;		
	}

	/**
	 * Runs the environment.
	 * 
	 * @return Stop time.
	 */
	public synchronized int run() {
		try {
			while(!jobs.isEmpty()) {
				step();
			}
		}
		catch(Exception ex) {
			while(!jobs.isEmpty()) {
				jobs.remove(0).event.terminate();
			}
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
		}
		catch(Exception ex) {
			while(!jobs.isEmpty()) {
				jobs.remove(0).event.terminate();
			}
		}
		return this.now;
	}

	/**
	 * Processes the next event.
	 * 
	 * @throws SimException 
	 * 
	 */
	private void step() throws SimException {
		// 1. get the first job.
		Job job = this.jobs.remove(0);
		// 2. update environment time
		this.now = job.time;
		logger.debug(String.format("ENV> STEP> %s> %s, callbacks(%s)", this.now, job.event, job.event.getNumberOfCallbables()));
		// 3. callback the event.
		job.event.callback();
		logger.debug(String.format("ENV> STEP> %s> %s, callbacks done", this.now, job.event));
		// 4. check if event is OK.
		if(!job.event.isOk() && !job.event.isDefused()) {
			throw new SimException(job.event, "failed to callback");
		}
	}
	
	private void stopSim(Event event) {
		throw new RuntimeException("stop the simulation");
	}
	
	private int sortJobs(Job a, Job b) {
		return a.time - b.time;
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
			return this.time - c2.time;
		}
		
		@Override
		public String toString() {
			return String.format("%s, time=%s", event, time);
		}
	}
}
