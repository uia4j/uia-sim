package uia.sim.resources;

import java.util.ArrayList;

import uia.sim.Env;

/**
 * A simple resource implementation.
 *
 * @author Kan
 *
 */
public class Resource extends BaseResource<Resource> {

    private ArrayList<Request> requests;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param capacity The capacity.
     */
    public Resource(Env env, int capacity) {
        super(env, capacity);
        this.requests = new ArrayList<>();
    }

    /**
     * Requests a resource.
     *
     * @param id The id.
     * @return The request event.
     */
    public Request request(String id) {
        return new Request(this, id);
    }

    /**
     * Release a resource.
     *
     * @param id The id.
     * @param request The request to be released.
     * @return The release event.
     */
    public Release release(String id, Request request) {
        return new Release(this, id, request);
    }

    @Override
    public boolean doRequest(BaseRequest<Resource> request) {
        if (this.requests.size() < this.capacity) {
            this.requests.add((Request) request);
            request.succeed(null);  // resume the process to work.
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean doRelease(BaseRelease<Resource> release) {
        this.requests.remove(release.request);
        release.succeed(null);      // resume the resource to refresh requests.
        return true;
    }

    /**
     * The request event of the simple resource.
     *
     * @author Kan
     *
     */
    public static class Request extends BaseRequest<Resource> {

        public Request(Resource resource, String id) {
            super(resource, id);
            this.resource.addRequest(this);
        }

        @Override
        public void exit() {
            this.resource.release(getId(), this);
        }
    }

    /**
     * The release event of the simple resource.
     *
     * @author Kan
     *
     */
    public static class Release extends BaseRelease<Resource> {

        public Release(Resource resource, String id, Request request) {
            super(resource, id + "_release", request);
            this.resource.addRelease(this);
        }

        @Override
        public void exit() {
        }
    }
}
