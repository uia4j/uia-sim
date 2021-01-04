package uia.sim;

import java.util.Vector;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Yield;

public class Env {
	
    private static final Logger logger = LogManager.getLogger(Env.class);

	private int now;
	
	private Vector<Job> jobs;
	
	private Process activeProcess;
	
	public Env() {
		this.jobs = new Vector<>();
	}

	public Env(int initialTime) {
		this.jobs = new Vector<>();
		this.now = Math.max(0, initialTime);
	}
	
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
	 * Returns a new process event for a generator..
	 * 
	 * @param c a Yield consumer.
	 * @return A new Process event.
	 */
	public Process process(String name, Consumer<Yield<Event>> consumer) {
		return new Process(this, name, consumer);
	}

	/**
	 * Returns a new timeout event with a delay.
	 * 
	 * @param delay The delay time.
	 * @return A new Timeout event.
	 */
	public Timeout timeout(int delay) {
		return new Timeout(this, delay);
	}
	
	/**
	 * Returns a new event instance.
	 * 
	 * @param name The event name.
	 * @return A new event.
	 */
	public Event event(String name) {
		return new Event(this, name);
	}

	public void schedule(Event event, Event.PriorityType priority, int delay) {
		Job job = new Job(event, priority, this.now + delay);
		int a = jobs.size();
		this.jobs.add(job);
		int b = jobs.size();
		jobs.sort(this::sortJobs);
		logger.debug(String.format("ENV> SCH> %s, (%s->%s)", job, a, b));
	}
	
	/**
	 * Returns the time of the next scheduled event.
	 * 
	 * @return
	 */
	public long peek() {
		return this.jobs.isEmpty() ? -1 : this.jobs.get(0).time;		
	}

	public synchronized void run() throws SimException {
		try {
			Thread.sleep(500);
		}
		catch(Exception ex) {
			
		}
		while(!jobs.isEmpty()) {
			step();
		}
	}
	
	/**
	 * Executes events until the given criterion until is met.
	 * 
	 * @param until The end time.
	 * @throws SimException 
	 */
	public synchronized void run(final int until) throws SimException {
		if(until < this.now) {
			throw new IllegalArgumentException(String.format("until(=%s) must be > the current simulation time.", until));
		}

		try {
			Thread.sleep(500);
		}
		catch(Exception ex) {
			
		}
		while(this.now < until && !jobs.isEmpty()) {
			step();
		}
	}

	/**
	 * Processes the next event.
	 * @throws SimException 
	 * 
	 */
	private void step() throws SimException {
		Job job = this.jobs.remove(0);
		this.now = job.time;
		logger.debug(String.format("ENV> STEP> %s> %s, callback %s", this.now, job.event, job.event.countCallables()));
		job.event.callback();
		logger.debug(String.format("ENV> STEP> %s> %s, callback done", this.now, job.event));
		
		if(!job.event.isOk()) {
			throw new SimException(job.event, "failed to callback");
		}
	}
	
	private int sortJobs(Job a, Job b) {
		return a.time - b.time;
	}
	
	class Job implements Comparable<Job> {
		
		public final Event event;
		
		public final Event.PriorityType priority;
		
		public final int time;

		Job(Event event, Event.PriorityType priority, int time) {
			this.event = event;
			this.priority = priority;
			this.time = time;
			
		}

		@Override
		public int compareTo(Job c2) {
			return this.time - c2.time;
		}
		
		public String toString() {
			return String.format("%s, time=%s", event, time);
		}
	}
}
