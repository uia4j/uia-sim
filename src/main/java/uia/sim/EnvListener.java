package uia.sim;

/**
 * The environment listener.
 * 
 * @author Kan
 *
 */
public interface EnvListener {

    public void stepDone(int time, String processId, Event event);

    public void stepFailed(int time, String processId, Event event);
}
