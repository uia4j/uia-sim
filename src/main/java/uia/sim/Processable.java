package uia.sim;

import uia.cor.Yield2Way;
import uia.sim.events.Process;

/**
 * The abstract processable class.<br>
 * It provides some help methods for inherited classes to clearly develop object-oriented style programs.
 *
 * <p>
 * The environment instance is only available after binding,
 * override the <b>postBind</b> method to configure the environment instead of in <b>constructor</b>.
 * </p>
 *
 * <p>
 * The example below inherits from the Processable and implement run().<br>
 * <ul>
 * <li>yield(value) submits an new event to the process.</li>
 * <li>env().timeout(10) creates a timeout event with 10 ticks delay.</li>
 * <li>now() gets the current time of the environment.</li>
 * </ul>

 * <pre>{@code
 * public Hello extends Processable {
 *
 *     public Hello() {
 *         super("DESimJava");
 *     }
 *
 *     public void run() {
 *         yield(env().timeout(10));
 *         System.out.println(now() + ", Hello " + getId());
 *     }
 * }
 *
 * Env env = new Env();
 * env.process(new Hello());
 * env.run();
 *
 * }</pre>
 *
 * Above example will output:
 * <pre>
 * 10, Hello DESimJava
 * </pre>
 * @author Kan
 *
 */
public abstract class Processable {

    private final String id;

    private Env env;

    private Process process;

    private Yield2Way<Event, Object> yield;

    /**
     * The constructor.
     *
     * @param id The process id.
     */
    protected Processable(String id) {
        this.id = id;
    }

    /**
     * Returns the process id.
     *
     * @return The process id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Bind the process with specific environment.<br>
     * Only allowed to bind once, or throw a runtime exception.
     *
     * @param env The environment.
     * @return A new process.
     */
    public final Process bind(Env env) {
        if (this.env != null) {
            throw new SimException(this.getId() + " has binded already");
        }

        this.env = env;
        this.process = env.process(this.id, this::readyToGo);
        initial();
        return this.process;
    }

    /**
     * Returns current time of the environment.
     *
     * @return The time.
     */
    protected int now() {
        return this.env.getNow();
    }

    /**
     * Returns the environment.
     *
     * @return The environment.
     */
    protected final Env env() {
        return this.env;
    }

    /**
     * Returns the process.
     *
     * @return The process.
     */
    protected final Process proc() {
        return this.process;
    }

    /**
     * Returns the yield control object.
     *
     * @return The yield.
     */
    protected final Yield2Way<Event, Object> yield() {
        return this.yield;
    }

    /**
     * Yields a event to the generator and get the result.
     *
     * @param event The event sent to the generator.
     * @return The result sent back from the generator.
     */
    protected final Object yield(Event event) {
        return this.yield.call(event);
    }

    /**
     * Invoked after binding. Override the method to configure the environment instead of in constructor.
     *
     */
    protected abstract void initial();

    /**
     * Runs this process.
     *
     */
    protected abstract void run();

    @Override
    public String toString() {
        return this.process.toString();
    }

    private void readyToGo(Yield2Way<Event, Object> yield) {
        this.yield = yield;
        run();
    }
}
