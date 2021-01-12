package uia.sim.resources;

import java.io.Closeable;
import java.io.IOException;

import uia.sim.Event;
import uia.sim.events.Process;

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
	public void close() throws IOException {
		exit();
		if(!isTriggered()) {
			this.resource.removeRequest(this);
		}
	}
	
	protected abstract void exit();
}
