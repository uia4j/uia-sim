package uia.sim.resources;

import java.io.Closeable;
import java.io.IOException;

import uia.sim.Event;
import uia.sim.events.Process;

/**
 * The abstract release control object of a resource.
 *
 * @author Kan
 *
 * @param <T> The resource.
 */
public abstract class BaseRelease<T extends BaseResource<T>> extends Event implements Closeable {

    protected final T resource;

    protected final Process process;

    protected final BaseRequest<T> request;

    protected BaseRelease(T resource, String id, BaseRequest<T> request) {
        super(resource.getEnv(), id);
        this.resource = resource;
        this.process = resource.getEnv().getActiveProcess();
        this.request = request;
    }

    @Override
    public void close() throws IOException {
        exit();
        if (!isTriggered()) {
            this.resource.removeRelease(this);
        }
    }

    /**
     * Invoked when the request will be closed.
     *
     */
    protected abstract void exit();
}
