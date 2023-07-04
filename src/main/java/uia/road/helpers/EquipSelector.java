package uia.road.helpers;

import java.util.Collections;
import java.util.List;

import uia.road.Equip;
import uia.road.Job;

/**
 * The jobs selector.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public interface EquipSelector<T> {

    /**
     * Select jobs for a specific equipment.
     *
     * @param job The requesting job.
     * @param equips The equipments can be selected.
     * @return Selected equipment.
     */
    public CandidateInfo<T> select(Job<T> job, List<Equip<T>> equips);

    /**
     * A simple job selector.
     *
     * @author Kan
     * @param <T> Reference data of the job.
     */
    public static class Any<T> implements EquipSelector<T> {

        @Override
        public CandidateInfo<T> select(Job<T> job, List<Equip<T>> equips) {
            return new CandidateInfo<>(equips, Collections.emptyList());
        }
    }

    /**
     * The result of selection.
     *
     * @author Kan
     * @param <T> Reference data of the job.
     */
    public static class CandidateInfo<T> {

        private final List<Equip<T>> passed;

        private final List<Equip<T>> ignore;

        public CandidateInfo(List<Equip<T>> passed, List<Equip<T>> ignore) {
            this.passed = passed;
            this.ignore = ignore;
        }

        /**
         * Returns equipments which can are loadable.
         *
         * @return The equipments.
         */
        public List<Equip<T>> getPassed() {
            return this.passed;
        }

        /**
         * Returns equipments which can are not loadable.
         *
         * @return The equipments.
         */
        public List<Equip<T>> getIgnore() {
            return this.ignore;
        }

    }
}
