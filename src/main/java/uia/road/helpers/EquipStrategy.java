package uia.road.helpers;

import uia.road.Equip;
import uia.road.Job;

/**
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public interface EquipStrategy<T> {

    public void update(Equip<T> equip, Job<T> job, String event);
}
