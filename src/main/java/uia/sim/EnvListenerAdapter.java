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
    public void stepDone(int time, String processId, Event event) {
        System.out.println(String.format("%4d> %s> %s done", time, processId, event));
    }

    @Override
    public void stepFailed(int time, String processId, Event event) {
        System.out.println(String.format("%4d> %s> %s failed", time, processId, event));
    }

}
