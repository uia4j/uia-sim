package uia.sim;

public class SimEventException extends RuntimeException {

    private static final long serialVersionUID = 6332759220723531098L;

    public final Event event;

    public SimEventException(Event event, String message) {
        super(message);
        this.event = event;
    }

    public SimEventException(Event event, String message, Throwable th) {
        super(message, th);
        this.event = event;
    }
}
