package uia.road.helpers;

import java.util.List;
import java.util.Random;

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
     * @param equip The requesting equipment.
     * @param jobs The jobs can be selected.
     * @return Selected jobs.
     */
    public Equip<T> select(Job<T> box, List<Equip<T>> equips);

    public static class Any<T> implements EquipSelector<T> {

        @Override
        public Equip<T> select(Job<T> job, List<Equip<T>> equips) {
            if (equips.isEmpty()) {
                return null;
            }

            return equips.get(new Random().nextInt(equips.size()));
        }
    }
}
