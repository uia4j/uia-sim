package uia.road.helpers;

import java.util.ArrayList;
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
public interface JobSelector<T> {

    /**
     * Select a job for a specific equipment.
     *
     * @param equip The requesting equipment.
     * @param jobs The jobs can be selected.
     * @return Selected job.
     */
    public CandidateInfo<T> select(Equip<T> equip, List<Job<T>> jobs);

    public static class Any<T> implements JobSelector<T> {

        @Override
        public CandidateInfo<T> select(Equip<T> equip, List<Job<T>> jobs) {
            if (!equip.isEnabled()) {
                return new CandidateInfo<>(null, jobs);
            }

            ArrayList<Job<T>> data = new ArrayList<>(jobs);
            Collections.shuffle(data);
            for (Job<T> job : data) {
                if (!job.isLoaded()) {
                    return new CandidateInfo<T>(job, data);
                }
            }
            return new CandidateInfo<T>(null, data);
        }
    }

    public static class CandidateInfo<T> {

        private Job<T> selected;

        private final List<Job<T>> passed;

        private final List<Job<T>> ignore;

        public CandidateInfo(Job<T> selected, List<Job<T>> passed) {
            this.selected = selected;
            this.passed = passed;
            this.ignore = new ArrayList<>();
        }

        public CandidateInfo(Job<T> selected, List<Job<T>> passed, List<Job<T>> ignore) {
            this.selected = selected;
            this.passed = passed;
            this.ignore = ignore;
        }

        public Job<T> getSelected() {
            return this.selected;
        }

        public void setSelected(Job<T> selected) {
            this.selected = selected;
        }

        public List<Job<T>> getPassed() {
            return passed;
        }

        public List<Job<T>> getIgnore() {
            return ignore;
        }

    }
}
