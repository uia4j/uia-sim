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

    public static class Any<T> implements EquipSelector<T> {

        @Override
        public CandidateInfo<T> select(Job<T> job, List<Equip<T>> equips) {
            if (equips.isEmpty()) {
                return new CandidateInfo<>(null, equips, Collections.emptyList());
            }
            return new CandidateInfo<>(equips.get(0), equips, Collections.emptyList());
        }
    }

    public static class CandidateInfo<T> {

        private Equip<T> selected;

        private final List<Equip<T>> passed;

        private final List<Equip<T>> ignore;

        public CandidateInfo(Equip<T> selected, List<Equip<T>> passed, List<Equip<T>> ignore) {
            this.selected = selected;
            this.passed = passed;
            this.ignore = ignore;
        }

        public Equip<T> getSelected() {
            return this.selected;
        }

        public void setSelected(Equip<T> selected) {
            this.selected = selected;
        }

        public List<Equip<T>> getPassed() {
            return this.passed;
        }

        public List<Equip<T>> getIgnore() {
            return this.ignore;
        }

    }
}
