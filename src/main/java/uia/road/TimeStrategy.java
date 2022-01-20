package uia.road;

public class TimeStrategy {

    private int from;

    private int to;

    public TimeStrategy() {
        this(0, Integer.MAX_VALUE);
    }

    public TimeStrategy(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return this.from;
    }

    public void setFrom(int from) {
        this.from = Math.max(this.from, from);
    }

    public int getTo() {
        return this.to;
    }

    public void setTo(int to) {
        this.to = Math.min(this.to, to);
    }

    public boolean checkTo() {
        return this.to < Integer.MAX_VALUE;
    }

    public void reset() {
        this.from = 0;
        this.to = Integer.MAX_VALUE;
    }

    public int remain(int now) {
        return this.to < Integer.MAX_VALUE
                ? this.to - now
                : Integer.MAX_VALUE;
    }
}
