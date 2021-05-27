package uia.sim;

/**
 * The environment listener.
 *
 * @author Kan
 *
 */
public interface EnvListener {

    public void init(String message);

    public void running(int time, String message);

    public void done(int time, String message);
}
