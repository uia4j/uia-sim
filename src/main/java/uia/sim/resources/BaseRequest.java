package uia.sim.resources;

import java.io.Closeable;

import uia.sim.Event;
import uia.sim.events.Process;

/**
 * The abstract request control object of a resource.
 *
 * @author Kan
 *
 * @param <T> The resource.
 */
public abstract class BaseRequest<T extends BaseResource<T>> extends Event implements Closeable {

    protected final T resource;

    protected final Process process;

    /**
     * The constructor.
     *
     * @param resource The resource.
     * @param id The request id.
     */
    protected BaseRequest(T resource, String id) {
        super(resource.getEnv(), id);
        this.resource = resource;
        this.process = resource.getEnv().getActiveProcess();
    }

    @Override
    public void close() {
        exit();
        if (!isTriggered()) {
            this.resource.removeRequest(this);
        }
    }

    /**
     * Invoked when the request will be closed.
     *
     */
    protected abstract void exit();
}
