package uia.road.utils;

public final class TimeFormat {

    public static String fromSec(int time) {
        int v = time < 0 ? -time : time;
        int hour = v / 3600;
        int min = (v - hour * 3600) / 60;
        int sec = v - hour * 3600 - min * 60;
        return String.format("%3d:%02d:%02d", time < 0 ? -hour : hour, min, sec);
    }

    public static String fromMin(int time) {
        int v = time < 0 ? -time : time;
        int day = v / 1440;
        int hour = (v - day * 1440) / 60;
        int min = (v - day * 1440 - hour * 60);
        return String.format("%3s %02d:%02d", time < 0 ? -day : day, hour, min);
    }
}
