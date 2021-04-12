package uia.road.utils;

public final class TimeFormat {

    private TimeFormat() {

    }

    public static String fromSec(int time) {
        int hour = time / 3600;
        int min = (time - hour * 3600) / 60;
        int sec = time - hour * 3600 - min * 60;
        return String.format("%3d:%02d:%02d", hour, min, sec);
    }

    public static String fromMin(int time) {
        int day = time / 1440;
        int hour = (time - day * 1440) / 60;
        int min = (time - day * 1440 - hour * 60);
        return String.format("%2s %02d:%02d", day, hour, min);
    }
}
