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
}
