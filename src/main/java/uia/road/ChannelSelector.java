package uia.road;

import java.util.List;
import java.util.Optional;

public interface ChannelSelector<T> {

    public Channel<T> select(List<Channel<T>> chs);

    public Channel<T> select(List<Channel<T>> chs, Job<T> job);

    public static class Any<T> implements ChannelSelector<T> {

        @Override
        public Channel<T> select(List<Channel<T>> chs) {
            Optional<Channel<T>> opts = chs.stream().filter(c -> !c.isProcessing()).findAny();
            return opts.isPresent() ? opts.get() : null;
        }

        @Override
        public Channel<T> select(List<Channel<T>> chs, Job<T> job) {
            return select(chs);
        }

    }
}
