package uia.sim;

public class SimException extends Exception {

    private static final long serialVersionUID = 6332759220723531098L;

    public final Event event;

    public SimException(Event event, String message) {
        super(message);
        this.event = event;
    }

    public SimException(Event event, Throwable th) {
        super(th);
        this.event = event;
    }
}
