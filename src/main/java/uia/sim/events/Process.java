package uia.sim.events;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Generator2Way;
import uia.cor.Yield2Way;
import uia.sim.Env;
import uia.sim.Event;
import uia.sim.SimException;

public class Process extends Event {
	
    private static final Logger logger = LogManager.getLogger(Process.class);

	private final Generator2Way<Event, Object> taskGen;
	
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
		this.target = new Initialize(this);
	}

	/**
	 * Interrupts this process.
	 * 
	 * @param cause The cause to interrupt this process.
	 */
	public synchronized void interrupt(Exception cause) {
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

	/**
	 * Resumes to execute the task (the most important part of this framework).
	 * 
	 * @param by The event resumes the process.
	 */
	public synchronized void resume(Event by) {
		String tx = UUID.randomUUID().toString().substring(0, 6);
		logger.debug(String.format("%s> resume(%s), from %s", getId(), tx, by.toFullString()));
		if(!by.isOk()) {
			logger.debug(String.format("%s> resume(%s), interrupt", getId(), tx));
			this.target = null;
			setValue(null);
			if(by.getValue() == null) {
				this.taskGen.interrupt(new SimException(this, this.id + " interrupted"));
			}
			else if(by.getValue() instanceof Exception) {
				this.taskGen.interrupt((Exception)by.getValue());
			}
			else {
				this.taskGen.interrupt(new SimException(this, by.getValue().toString()));
			}
			return;
		}
		
		this.env.setActiveProcess(this);
		
		Event event = by;
		// 回傳  event.value 給前一次的 yield，並檢查是否有新的 yield。
		// 接力傳送？這真是奇了！
		while(this.taskGen.next(event.getValue())) {
			// 取得最新 yield 過來的  event。
			event = taskGen.getValue();
			logger.debug(String.format("%s> resume(%s), next %s", getId(), tx, event.toFullString()));
			// 檢查 event 是否未處理
			if(!event.isProcessed()) {
				// 將此 process 的 resume 流程掛載至 event 上。
				event.addCallable(this::resume);
				this.target = event;
				// 中斷，等下一次 resume。
				break;
			}
		}
		logger.debug(String.format("%s> resume(%s), done", getId(), tx));
		
		if(this.taskGen.isClosed()) {
			this.target = null;
			setValue(null);
		}
		this.env.setActiveProcess(null);
	}
}
