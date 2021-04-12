package uia.road.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * Select jobs for a specific equipment.
     * 
     * @param equip The requesting equipment.
     * @param jobs The jobs can be selected.
     * @return Selected jobs.
     */
    public SelectResult<T> select(Equip<T> equip, List<Job<T>> jobs);

    public static class SelectResult<T> {

        private final String groupId;

        private final List<Job<T>> jobs;

        public SelectResult() {
            this.groupId = null;
            this.jobs = new ArrayList<>();
        }

        public SelectResult(String groupId, List<Job<T>> jobs) {
            this.groupId = groupId;
            this.jobs = jobs;
        }

        public SelectResult(Job<T> job) {
            this.groupId = job.getBoxId();
            this.jobs = Arrays.asList(job);
        }

        public String getGroupId() {
            return this.groupId;
        }

        public List<Job<T>> getJobs() {
            return this.jobs;
        }

        public boolean isEmpty() {
            return this.jobs.isEmpty();
        }
    }

    public static class FIFO<T> implements JobSelector<T> {

        @Override
        public SelectResult<T> select(Equip<T> equip, List<Job<T>> jobs) {
            if (jobs.isEmpty()) {
                return new SelectResult<>();
            }

            String boxId = jobs.get(0).getBoxId();
            List<Job<T>> selected = jobs.stream()
                    .filter(j -> boxId.equals(j.getBoxId()))
                    .collect(Collectors.toList());
            return new SelectResult<>(boxId, selected);
        }
    }
}
