package uia.sim.resources;

import uia.sim.Env;
import uia.sim.SimException;

/**
 * A amount based container.
 *
 * @author Kan
 *
 */
public final class Container extends BaseResource<Container> {

    private int amount;

    /**
     * The constructor.
     *
     * @param env The environment.
     * @param initialAmount The initial amount could be requested.
     */
    public Container(Env env, int initialAmount) {
        super(env);
        this.amount = initialAmount;
    }

    /**
     * Requests a amount from the container.
     *
     * @param id The request id.
     * @param planUsage Amount the request plans to use.
     * @return The request event.
     */
    public Request request(String id, int planUsage) throws SimException {
        return new Request(this, id, planUsage);
    }

    /**
     * Adds new amount to the container.
     *
     * @param id The release id.
     * @param amount Amount to be added to the container.
     * @return The release event.
     */
    public Release release(String id, int amount) {
        return new Release(this, id, amount);
    }

    @Override
    protected synchronized boolean doRequest(BaseRequest<Container> request) {
        int planUsage = ((Request) request).planUsage;
        if (this.amount - planUsage >= 0) {
            this.amount -= planUsage;
            request.succeed(null);      // resume the process to work.
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected synchronized boolean doRelease(BaseRelease<Container> release) {
        this.amount += ((Release) release).amount;
        release.succeed(null);      // resume the resource to refresh requests.
        return true;
    }

    /**
     * The request event of the container.
     *
     * @author Kan
     *
     */
    public static final class Request extends BaseRequest<Container> {

        public final int planUsage;

        private int remaining;

        /**
         * The constructor.
         *
         * @param container The container.
         * @param id The request id.
         * @param planUsage Amount the request plans to use.
         */
        protected Request(Container container, String id, int planUsage) {
            super(container, id);
            this.planUsage = planUsage;
            this.resource.addRequest(this);
        }

        /**
         * Sets the actual amount the request uses.
         *
         * @param amount The amount.
         */
        public void consume(int amount) {
            this.remaining = Math.max(0, this.planUsage - amount);
        }

        @Override
        public void exit() {
            if (this.remaining > 0) {
                this.resource.release(this.id, this.remaining);
            }
        }
    }

    /**
     * The release event of the container.
     *
     * @author Kan
     *
     */
    public static final class Release extends BaseRelease<Container> {

        /**
         * The amount to be added to the container.
         */
        public final int amount;

        /**
         * The constructor.
         *
         * @param container The container.
         * @param id The release id.
         * @param amount The amount to be added.
         */
        protected Release(Container container, String id, int amount) {
            super(container, id);
            this.amount = amount;
            this.resource.addRelease(this);
        }

        @Override
        public void exit() {
        }
    }
}
