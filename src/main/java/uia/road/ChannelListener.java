package uia.road;

public interface ChannelListener<T> {

    public void processEnded(Channel<T> channl, Job<T> job, int qty);
}
