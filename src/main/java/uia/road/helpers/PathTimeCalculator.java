package uia.road.helpers;

import uia.road.Op;

/**
 * The path time calculator.
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public interface PathTimeCalculator<T> {

    public int calc(Op<T> from, Op<T> to);

    public static class Simple<T> implements PathTimeCalculator<T> {

        private final int time;

        public Simple(int time) {
            this.time = time;
        }

        @Override
        public int calc(Op<T> from, Op<T> to) {
            return this.time;
        }

    }
}
