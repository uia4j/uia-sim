package uia.road;

/**
 * The working channel in the equipment.
 *
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public interface Channel<T> {

    public int getMaxProcessTime();

    public void setMaxProcessTime(int maxProcessTime);

    public int getBatchSize();

    public void setBatchSize(int batchSize);

    public int getCompensationTime();

    public void setCompensationTime(int compensationTime);

    public SimInfo getInfo();

    public boolean isProcessing();

    public boolean run(Job<T> job) throws Exception;
}
