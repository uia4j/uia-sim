package uia.sim.events;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.cor.Generator2Way;
import uia.cor.Yield2Way;
import uia.cor.Yieldable2Way;
import uia.sim.Env;
import uia.sim.Event;
import uia.sim.SimEventException;

/**
 * Process controller.<br>
 * <p>
 * The process uses a generator to work together with iteration program.
 * </p>
 *
 * @author Kan
 *
 */
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
        this.target = new Initialize(this);	// used to startup the resume()
    }

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param eventId The event id.
     * @param taskRunner The task to be executed.
     */
    public Process(Env env, String eventId, Yieldable2Way<Event, Object> taskRunner) {
        super(env, eventId);
        this.taskGen = Yield2Way.accept(eventId, taskRunner);
        this.resumeCallable = new Consumer<Event>() {

            @Override
            public void accept(Event event) {
                Process.this.resume(event);
            }
        };
        this.target = new Initialize(this);	// used to startup the resume()
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

    /**
     * Tests if the process is running(tasks iterating) or not.
     *
     * @return True is the process is running.
     */
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
     * Adds resume() of this process to the callable of the specific event.
     *
     * @param event The event.
     * @return Successful or not.
     */
    public boolean bind(Event event) {
        if (event == null) {
            return false;
        }
        return event.addCallable(this.resumeCallable);
    }

    /**
     * Removes resume() of this process from the callable of the specific event.
     *
     * @param event The event.
     * @return Successful or not.
     */
    public boolean unbind(Event event) {
        if (event == null) {
            return false;
        }
        return event.removeCallable(this.resumeCallable);
    }

    /**
     * Resumes to execute the next ONE event which the state is 'waiting'.<br>
     *
     * This is the  most important part of this framework).
     *
     * @param by The event resumes the process.
     */
    public synchronized void resume(Event by) {
        if (this.taskGen.isClosed()) {
            logger.debug(String.format("%4d> %s> resume(closed), by %s", this.env.getNow(), getId(), by.toFullString()));
            return;
        }

        if (by.isEnvDown()) {
            logger.debug(String.format("%4d> %s> resume(envDown), by %s", this.env.getNow(), getId(), by.toFullString()));
            this.taskGen.stop(new InterruptedException("envDown"));
            return;
        }

        final String tx = UUID.randomUUID().toString().substring(0, 6);
        logger.debug(String.format("%4d> %s> resume(%s), by %s", this.env.getNow(), getId(), tx, by.toFullString()));

        this.env.setActiveProcess(this);
        Event event = by;
        boolean next = true;
        while (next) {
            if (!event.isOk()) {
                event.defused();
                // this.env.raiseProcessFailed(this.env.getNow(), getId(), event);

                // 回傳  exception 給前一次的 yield，並檢查是否有新的 yield。
                if (event.getValue() == null) {
                    next = this.taskGen.errorNext(new SimEventException(this, this.id + " interrupted"));
                }
                else if (event.getValue() instanceof Exception) {
                    Exception ex = (Exception) event.getValue();
                    next = this.taskGen.errorNext(new SimEventException(this, ex.getMessage(), ex));
                }
                else {
                    next = this.taskGen.errorNext(new SimEventException(this, event.getValue().toString()));
                }
            }
            else {
                // this.env.raiseProcessDone(this.env.getNow(), getId(), event);

                // 回傳  event.value 給前一次的 yield，並檢查是否有新的 yield。
                next = this.taskGen.next(event.getValue());
            }
            if (next) {
                event = this.taskGen.getValue();
                // 檢查 event 是否未處理
                if (!event.isProcessed()) {
                    // 關鍵：將此 process 的 resume 流程掛載至 event 上。
                    event.addCallable(this.resumeCallable);
                    // 中斷，等下一次 resume。
                    logger.debug(String.format("%4d> %s> resume(%s), %s, blocking", this.env.getNow(), getId(), tx, event));
                    break;
                }
                else {
                    logger.debug(String.format("%4d> %s> resume(%s), %s, processed", this.env.getNow(), getId(), tx, event));
                }
            }
        }
        this.target = event;
        this.env.setActiveProcess(null);

        if (!next) {
            logger.info(String.format("%4d> %s> will be closed", this.env.getNow(), this, tx));
            succeed(this.taskGen.getFinalResult());
        }
    }

    @Override
    public String toString() {
        return "Proc(" + getId() + ")";
    }
}
