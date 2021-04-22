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
    public Job<T> select(Equip<T> equip, List<Job<T>> jobs);

    public static class Any<T> implements JobSelector<T> {

        @Override
        public Job<T> select(Equip<T> equip, List<Job<T>> jobs) {
            ArrayList<Job<T>> data = new ArrayList<>(jobs);
            Collections.shuffle(data);
            for (Job<T> job : data) {
                if (!job.isLoaded()) {
                    return job;
                }
            }
            return null;
        }
    }
}
