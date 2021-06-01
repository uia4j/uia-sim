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

    public int calc(Equip<T> equip, Job<T> job);

    public static class Simple<T> implements ProcessTimeCalculator<T> {

        private final int pt;

        public Simple(int pt) {
            this.pt = pt;
        }

        @Override
        public int calc(Equip<T> equip, Job<T> job) {
            return pt;
        }

    }
}
