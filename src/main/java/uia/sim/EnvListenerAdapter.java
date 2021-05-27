package uia.sim;

/**
 * The environment listener adapter<br>
 *
 * <p>
 * The implementation will call System.out.println to print some information.
 * </p>
 *
 * @author Kan
 *
 */
public class EnvListenerAdapter implements EnvListener {

    @Override
    public void init(String message) {
        System.out.println(String.format("init: %s", message));
    }

    @Override
    public void running(int time, String message) {
        System.out.println(String.format("run:  %s", message));
    }

    @Override
    public void done(int time, String message) {
        System.out.println(String.format("done: %s", message));
    }

}
