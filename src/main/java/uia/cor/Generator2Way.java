package uia.cor;

/**
 * The generator pairs with Yeild2Way.
 *
 * @author Kan
 *
 * @param <T> The data type exchanges to the generator.
 * @param <R> The data type the generator sends back.
 */
public class Generator2Way<T, R> {

    private final Yield2Way<T, R> yield;

    /**
     * The constructor.
     *
     * @param yield The yield object.
     */
    public Generator2Way(Yield2Way<T, R> yield) {
        this.yield = yield;
    }

    /**
     * Reports error to the iteration.
     *
     * @param message The error message.
     */
    public synchronized void error(String message) {
        error(new YieldException(message));
    }

    /**
     * Reports error to the iteration.
     *
     * @param cause The cause.
     */
    public synchronized void error(Exception cause) {
        this.yield.error(cause);
    }

    /**
     * Sends a result to the iteration.
     *
     * @param callResult The result.
     */
    public synchronized void send(R callResult) {
        this.yield.send(callResult);
    }

    /**
     * Checks if there is a next iteration or not.<br>
     *
     * @return True if there is a next iteration.
     */
    public synchronized boolean next() {
        return this.yield.next(false);
    }

    /**
     * Checks if there is a next iteration or not.<br>
     * The method calls 2 methods: send(value), next().
     *
     * @param callResult The result for previous call().
     * @return True if there is a next iteration.
     */
    public synchronized boolean next(R callResult) {
        this.yield.send(callResult);
        return this.yield.next(false);
    }

    /**
     * Returns the last result and tests if there is a next iteration or not.
     *
     * @param callResult The result sent back to the iteration.
     * @return The next information.
     */
    public synchronized NextResult<T> nextResult(R callResult) {
        this.yield.send(callResult);
        boolean hasNext = this.yield.next(false);
        return new NextResult<T>(hasNext, this.yield.getValue());
    }

    /**
     * Checks if there is a next iteration or not.<br>
     * The method calls 2 methods: error(value), next().
     *
     * @param cause The cause.
     * @return True if there is a next iteration.
     */
    public synchronized boolean errorNext(Exception cause) {
        this.yield.error(cause);
        return this.yield.next(false);
    }

    /**
     * Returns the last result and tests if there is a next iteration or not.
     *
     * @param cause The cause.
     * @return The next information.
     */
    public synchronized NextResult<T> errorNextResult(Exception cause) {
        this.yield.error(cause);
        boolean hasNext = this.yield.next(false);
        return new NextResult<T>(hasNext, this.yield.getValue());
    }

    /**
     * Returns the current value of the iteration.
     *
     * @return The current value.
     */
    public synchronized T getValue() {
        return this.yield.getValue();
    }

    /**
     * Returns the final result of the iteration.
     *
     * @return The result.
     */
    public R getFinalResult() {
        return this.yield.getResult();
    }

    /**
     * Stops the iteration with a value successfully.
     *
     * @param callResult The result for previous call().
     */
    public synchronized void stop(R callResult) {
        this.yield.send(callResult);
        this.yield.next(true);
    }

    /**
     * Stops the iteration with a cause.
     *
     * @param cause The cause.
     */
    public synchronized void stop(Exception cause) {
        this.yield.error(cause);
        this.yield.next(true);
    }

    /**
     * Tests if the iteration is closed.
     *
     * @return True if the iteration is closed.
     */
    public boolean isClosed() {
        return this.yield.isClosed();
    }
}
