package uia.sim.events;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Generator2Way;
import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;

public class Process extends Event {
	
    private static final Logger logger = LogManager.getLogger(Process.class);

	private final Generator2Way<Event, Object> taskGen;
	
	private final Consumer<Event> resumeCallable;
	
	private Event target;
	
	/**
	 * The constructor.
	 * 
	 * @param env The environment.
	 * @param eventId The event id.
	 * @param taskRunner The task to be executed.
	 */
	public Process(Env env, String eventId, Consumer<Yield2Way<Event, Object>> taskRunner) {
		super(env, eventId);
		this.taskGen = Yield2Way.accept(eventId, taskRunner);
		this.resumeCallable = new Consumer<Event>() {

			@Override
			public void accept(Event event) {
				Process.this.resume(event);
			}
		};
		this.target = new Initialize(this);
	}

	/**
	 * Interrupts this process.<br>
	 * 
	 * <p>
	 * It will schedule a Interruption event for the process into the environment
	 * </p>
	 * 
	 * @param cause The cause to interrupt this process.
	 */
	public void interrupt(Exception cause) {
		logger.debug(String.format("%s> interrupt", getId()));
		Interruption.schedule(this, cause);
	}
	

	/**
	 * Interrupts this process.<br>
	 * 
	 * <p>
	 * It will schedule a Interruption event for the process into the environment
	 * </p>
	 * 
	 * @param cause The cause to interrupt this process.
	 */
	public void interrupt(String cause) {
		logger.debug(String.format("%s> interrupt", getId()));
		Interruption.schedule(this, cause);
	}

	public boolean isAlive() {
		return !this.taskGen.isClosed();
	}
	
	/**
	 * Returns The event that the process is currently waiting for.
	 * 
	 * @return The event.
	 */
	public Event getTarget() {
		return this.target;
	}

	
	public boolean bind(Event event) {
		return event.addCallable(this.resumeCallable);
	}

	public boolean unbind(Event event) {
		return event.removeCallable(this.resumeCallable);
	}

	@Override
	public String toString() {
		return "Proc(" + getId() + ")";
	}

	/**
	 * Resumes to execute the task (the most important part of this framework).
	 * 
	 * @param by The event resumes the process.
	 */
	public synchronized void resume(Event by) {
		if(this.taskGen.isClosed()) {
			logger.debug(String.format("%s> resume(closed), from %s", getId(), by.toFullString()));
			return;
		}

		if(by.isEnvDown()) {
			logger.debug(String.format("%s> resume(envDown), from %s", getId(), by.toFullString()));
			this.taskGen.interrupt("envDown");
			return;
		}

		final String tx = UUID.randomUUID().toString().substring(0, 6);
		logger.debug(String.format("%s> resume(%s), from %s", getId(), tx, by.toFullString()));

		this.env.setActiveProcess(this);
		Event event = by;
		boolean next = true;
		while(next) {
			if(!event.isOk()) {
				logger.debug(String.format("%s> resume(%s), %s NG", getId(), tx, event));
				event.defused();
				// 回傳  exception 給前一次的 yield，並檢查是否有新的 yield。
				if(event.getValue() == null) {
					next = this.taskGen.interrupt(new InterruptedException(this.id + " interrupted"));
				}
				else if(event.getValue() instanceof Exception) {
					Exception ex = (Exception)event.getValue();
					next = this.taskGen.interrupt(new InterruptedException(ex.getMessage()));
				}
				else {
					next = this.taskGen.interrupt(new InterruptedException(event.getValue().toString()));
				}
			}
			else {
				// 回傳  event.value 給前一次的 yield，並檢查是否有新的 yield。
				next = this.taskGen.next(event.getValue());
			}
			if(next) {
				event = this.taskGen.getValue();
				logger.debug(String.format("%s> resume(%s), next %s", getId(), tx, event.toFullString()));
	
				// 檢查 event 是否未處理
				if(!event.isProcessed()) {
					// 關鍵：將此 process 的 resume 流程掛載至 event 上。
					event.addCallable(this.resumeCallable);
					// 中斷，等下一次 resume。
					break;
				}
			}
		}
		this.target = event;
		this.env.setActiveProcess(null);
		
		if(!next) {
			this.env.schedule(this, PriorityType.NORMAL);
			succeed(this.taskGen.getResult());
			logger.debug(String.format("%s> resume(%s), closed", getId(), tx));
		}
		else {
			logger.debug(String.format("%s> resume(%s), done", getId(), tx));
		}
	}
}
