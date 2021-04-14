package uia.sim;

public class SimStopException extends SimException {

    private static final long serialVersionUID = -8597402088323899962L;

    public SimStopException() {
        super("stop the simulation");
    }

}
