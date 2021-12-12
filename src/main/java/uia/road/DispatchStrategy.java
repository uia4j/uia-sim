package uia.road;

public interface DispatchStrategy<T> {

    public void replan(Job<T> job);

    public static class Same<T> implements DispatchStrategy<T> {

        @Override
        public void replan(Job<T> job) {
        }

    }
}
