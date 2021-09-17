package uia.road.helpers;

import uia.road.Equip;
import uia.road.Job;

/**
 * The process time calculator.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public interface ProcessTimeCalculator<T> {

    public TimeInfo calc(Equip<T> equip, Job<T> job);

    public static class Simple<T> implements ProcessTimeCalculator<T> {

        private final TimeInfo pt;

        public Simple(int pt) {
            this.pt = new TimeInfo(pt);
        }

        @Override
        public TimeInfo calc(Equip<T> equip, Job<T> job) {
            return pt;
        }

    }

    public static class TimeInfo {

        public final int total;

        public final int nextAllowed;

        public TimeInfo(int total) {
            this.total = total;
            this.nextAllowed = total;
        }

        public TimeInfo(int total, int nextAllowed) {
            this.total = total;
            this.nextAllowed = nextAllowed;
        }
    }
}
