package uia.sim.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uia.sim.Env;
import uia.sim.Event;
import uia.sim.SimException;

/**
 * The abstract resource.
 *
 * <p>
 * The program just arranges <b>ALL</b> the requests and releases events <b>WITHOUT</b> capacity control.
 * The capacity control delegates to the sub classes by invoking <b>doRequest</b> and <b>doRelease</b> methods.
 * </p>
 *
 * @author Kan
 *
 * @param <T> The resource.
 */
public abstract class BaseResource<T extends BaseResource<T>> {

    private static final Logger logger = LogManager.getLogger(BaseResource.class);

    protected final Env env;

    protected final int capacity;

    private final List<BaseRequest<T>> requestQueue;

    private final List<BaseRelease<T>> releaseQueue;

    private Consumer<Event> dispatchRequestsCallable;

    private Consumer<Event> dispatchReleasesCallable;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param capacity The capacity.
     */
    public BaseResource(Env env, int capacity) {
        this.env = env;
        this.capacity = capacity;
        this.requestQueue = new ArrayList<>();
        this.releaseQueue = new ArrayList<>();
        this.dispatchRequestsCallable = this::dispatchRequests;
        this.dispatchReleasesCallable = this::dispatchReleases;
    }

    /**
     * Returns the environment.
     *
     * @return The environment.
     */
    public Env getEnv() {
        return this.env;
    }

    /**
     * Add a request event in this resource.
     *
     * @param request event The request event.
     */
    protected void addRequest(BaseRequest<T> request) {
        this.requestQueue.add(request);
        request.addCallable(this.dispatchReleasesCallable);  // invoked when this event is processed
        dispatchRequests(request);					//
    }

    /**
     * Remove a request event from this resource.
     *
     * @param request The request event.
     */
    protected void removeRequest(BaseRequest<T> request) {
        this.requestQueue.remove(request);
    }

    /**
     * Adds a release event in the resource
     * @param release A release event.
     */
    protected void addRelease(BaseRelease<T> release) {
        this.releaseQueue.add(release);
        release.addCallable(this.dispatchRequestsCallable);	// invoked when this event is processed
        dispatchReleases(release);
    }

    /**
     * Removes a release event from the resource.
     * @param release A release event.
     */
    protected void removeRelease(BaseRelease<T> release) {
        this.releaseQueue.remove(release);
    }

    /**
     * Invoked when a new request event is added in this resource.
     *
     * @param request A new request event.
     * @return Handling result.
     */
    protected abstract boolean doRequest(BaseRequest<T> request);

    /**
     * Invoked when a new release event is added in this resource.
     *
     * @param release A new release event.
     * @return Handling result.
     */
    protected abstract boolean doRelease(BaseRelease<T> release);

    /**
     * This method is called once a new <b>request</b> event has been added
     * or a <b>release</b> event has been processed.<br>
     *
     * @param by
     */
    private void dispatchRequests(Event by) {
        // 對累績的 requests 進行檢查。
        int idx = 0;
        while (idx < this.requestQueue.size()) {
            BaseRequest<T> request = this.requestQueue.get(idx);
            // request 申請  resource 占用
            boolean proceed = doRequest(request);
            logger.debug(String.format("res> scanRequest(%s)> %s, triggered:%s, proceed:%s",
                    by,
                    request,
                    request.isTriggered(),
                    proceed));
            if (!request.isTriggered()) {
                idx++;
            }
            else {
                // 將已經觸發完成的 request 移出佇列。
                if (this.requestQueue.remove(idx) != request) {
                    throw new SimException("request queue invariant violated");
                }
            }

            // 當 request 無法處理，視同資源耗盡，中斷檢查作業。
            if (!proceed) {
                break;
            }
        }
    }

    /**
     * This method is called once a new <b>release</b> event has been added
     * or a <b>request</b> event has been processed.<br>
     *
     * @param by The event.
     */
    private void dispatchReleases(Event by) {
        // 對累績的 release 進行檢查。
        int idx = 0;
        while (idx < this.releaseQueue.size()) {
            BaseRelease<T> release = this.releaseQueue.get(idx);
            // 進行 release 作業
            // release 歸還  resource 占用
            boolean proceed = doRelease(release);
            logger.debug(String.format("res> scanRelease(%s)> %s, triggered:%s, proceed:%s",
                    by,
                    release,
                    release.isTriggered(),
                    proceed));
            if (!release.isTriggered()) {
                idx++;
            }
            else {
                // 將已經觸發完成的 release 移出佇列。
                if (this.releaseQueue.remove(idx) != release) {
                    throw new SimException("release queue invariant violated");
                }
                release.close();
            }

            if (!proceed) {
                break;
            }
        }
    }

}
