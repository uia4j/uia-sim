package uia.sim.resources;

import java.util.ArrayList;

import uia.sim.Env;

/**
 * A capacity based resource.
 *
 * @author Kan
 *
 */
public final class Resource extends BaseResource<Resource> {

    private final int capacity;

    private final ArrayList<Request> requests;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param capacity The capacity.
     */
    public Resource(Env env, int capacity) {
        super(env);
        this.capacity = capacity;
        this.requests = new ArrayList<>();
    }

    /**
     * Requests a resource.
     *
     * @param id The request id.
     * @return The request event.
     */
    public Request request(String id) {
        return new Request(this, id);
    }

    /**
     * Release a resource.
     *
     * @param id The release id.
     * @param request The request to be released.
     * @return The release event.
     */
    public Release release(String id, Request request) {
        return new Release(this, id, request);
    }

    @Override
    protected boolean doRequest(BaseRequest<Resource> request) {
        if (this.requests.size() < this.capacity) {
            this.requests.add((Request) request);
            request.succeed(null);      // resume the process to work.
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected boolean doRelease(BaseRelease<Resource> release) {
        this.requests.remove(((Release) release).request);
        release.succeed(null);      // resume the resource to refresh requests.
        return true;
    }

    /**
     * The request event of the simple resource.
     *
     * @author Kan
     *
     */
    public static final class Request extends BaseRequest<Resource> {

        /**
         * The constructor.
         *
         * @param resource The resource.
         * @param id The request id.
         */
        protected Request(Resource resource, String id) {
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
    public static final class Release extends BaseRelease<Resource> {

        /**
         * The request which pairs with this release.
         */
        public final Request request;

        /**
         * The constructor.
         *
         * @param resource The resource.
         * @param id The release id.
         * @param request The request which pairs with this release.
         */
        protected Release(Resource resource, String id, Request request) {
            super(resource, id + "_release");
            this.request = request;
            this.resource.addRelease(this);
        }

        @Override
        public void exit() {
        }
    }
}
