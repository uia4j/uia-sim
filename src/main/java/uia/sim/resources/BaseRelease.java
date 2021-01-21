package uia.sim.resources;

import java.io.Closeable;

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

    protected BaseRelease(T resource, String id) {
        super(resource.getEnv(), id);
        this.resource = resource;
        this.process = resource.getEnv().getActiveProcess();
    }

    @Override
    public void close() {
        exit();
        this.resource.removeRelease(this);
    }

    /**
     * Invoked when the request will be closed.
     *
     */
    protected abstract void exit();
}
