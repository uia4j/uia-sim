package uia.cor;

/**
 * The exception of the library.
 * 
 * @author Kan
 *
 */
public class YieldException extends RuntimeException {

    private static final long serialVersionUID = -6230714829511628983L;

    /**
     * The constructor.
     * 
     * @param message The error message.
     */
    public YieldException(String message) {
        super(message);
    }

    /**
     * The constructor.
     * 
     * @param message The error message.
     * @param th The cause.
     */
    public YieldException(String message, Throwable th) {
        super(message, th);
    }
}
