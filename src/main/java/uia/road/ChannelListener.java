package uia.road;

public interface ChannelListener<T> {

	public void processStarted(Channel<T> channl, Job<T> job);
	
	public void processEnded(Channel<T> channl, Job<T> job);
}
