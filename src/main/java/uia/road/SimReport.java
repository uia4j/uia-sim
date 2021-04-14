package uia.road;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uia.road.events.EquipEvent;
import uia.road.events.Event;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.utils.TimeFormat;

/**
 * Simulation report.
 * 
 * @author Kan
 *
 */
public class SimReport {

    private final List<OpEvent> opEvents;

    private final List<EquipEvent> equipEvents;

    private final List<JobEvent> jobEvents;

    private IntFunction<String> timeFormat;

    private Gson gson;

    public SimReport() {
        this.opEvents = new ArrayList<>();
        this.equipEvents = new ArrayList<>();
        this.jobEvents = new ArrayList<>();
        this.timeFormat = TimeFormat::fromSec;
        this.gson = new GsonBuilder().create();
    }

    public void printlnOpEvents(boolean group) {
        if (group) {
            this.opEvents.stream()
                    .collect(Collectors.groupingBy(e -> e.getOperation()))
                    .forEach((k, v) -> {
                        Collections.sort(v, this::sort);
                        System.out.println(k);
                        v.forEach(l -> println(l));
                    });
        }
        else {
            this.opEvents.forEach(l -> println(l));
        }
    }

    public void printlnOpEvents(String id) {
        System.out.println(id);
        List<OpEvent> result = this.opEvents.stream()
                .filter(o -> id.equals(o.getOperation()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));
    }

    public void printlnEquipEvents(boolean group) {
        if (group) {
            this.equipEvents.stream()
                    .collect(Collectors.groupingBy(e -> e.getEquip()))
                    .forEach((k, v) -> {
                        Collections.sort(v, this::sort);
                        System.out.println(k);
                        v.forEach(l -> println(l));
                    });
        }
        else {
            this.equipEvents.forEach(l -> println(l));
        }
    }

    public void printlnEquipEvents(String id) {
        System.out.println(id);
        List<EquipEvent> result = this.equipEvents.stream()
                .filter(e -> id.equals(e.getEquip()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));

    }

    public void printlnJobEvents(boolean group) {
        if (group) {
            this.jobEvents.stream()
                    .collect(Collectors.groupingBy(e -> e.getProduct()))
                    .forEach((k, v) -> {
                        Collections.sort(v, this::sort);
                        System.out.println(k);
                        v.forEach(l -> println(l));
                    });
        }
        else {
            this.jobEvents.forEach(l -> println(l));
        }
    }

    public void printlnJobEvents(String id) {
        System.out.println(id);
        List<JobEvent> result = this.jobEvents.stream()
                .filter(j -> id.equals(j.getProduct()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));
    }

    public void printlnSimpleOpEvents() {
        this.opEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getOperation()))
                .forEach((k, v) -> {
                    Collections.sort(v, this::sort);
                    System.out.println(k);
                    v.forEach(l -> printlnSimple(l));
                });
    }

    public void printlnSimpleEquipEvents() {
        this.equipEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getEquip()))
                .forEach((k, v) -> {
                    Collections.sort(v, this::sort);
                    System.out.println(k);
                    v.forEach(l -> printlnSimple(l));
                });
    }

    public void printlnSimpleJobEvents() {
        this.jobEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getProduct()))
                .forEach((k, v) -> {
                    Collections.sort(v, this::sort);
                    System.out.println(k);
                    v.forEach(l -> printlnSimple(l));
                });
    }

    public List<OpEvent> getOpLogs() {
        return this.opEvents;
    }

    public List<EquipEvent> getEquipLogs() {
        return this.equipEvents;
    }

    public List<JobEvent> getJobLogs() {
        return this.jobEvents;
    }

    public void log(OpEvent e) {
        this.opEvents.add(e);
    }

    public void log(EquipEvent e) {
        this.equipEvents.add(e);
    }

    public void log(JobEvent e) {
        this.jobEvents.add(e);
    }

    public String toTimeString(int time) {
        return this.timeFormat.apply(time);
    }

    private void println(Event l) {
        System.out.println(String.format("%8s %8d %-15s - %s",
                this.timeFormat.apply(l.getTime()),
                l.getTime(),
                l.getEvent(),
                this.gson.toJson(l)));
    }

    private void printlnSimple(Event l) {
        System.out.println(String.format("%8s %8s",
                this.timeFormat.apply(l.getTime()),
                l));
    }

    private int sort(Event e1, Event e2) {
        return e1.getTime() - e2.getTime();
    }
}
