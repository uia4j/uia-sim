package uia.road;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uia.road.events.EquipEvent;
import uia.road.events.Event;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;

/**
 * Simulation report.
 *
 * @author Kan
 *
 */
public class SimReportTextLogger implements SimReportLogger {

    protected Gson gson;

    protected final List<OpEvent> opEvents;

    protected final List<EquipEvent> equipEvents;

    protected final List<JobEvent> jobEvents;

    protected final Factory<?> factory;

    public SimReportTextLogger(Factory<?> factory) {
        this.gson = new GsonBuilder().create();
        this.opEvents = new Vector<>();
        this.equipEvents = new Vector<>();
        this.jobEvents = new Vector<>();
        this.factory = factory;
    }

    @Override
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

    @Override
    public void printlnOpEvents(String id) {
        System.out.println(id);
        List<OpEvent> result = this.opEvents.stream()
                .filter(o -> id.equals(o.getOperation()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));
    }

    @Override
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

    @Override
    public void printlnEquipEvents(String id) {
        System.out.println(id);
        List<EquipEvent> result = this.equipEvents.stream()
                .filter(e -> id.equals(e.getEquip()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));

    }

    @Override
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

    @Override
    public void printlnJobEvents(String id) {
        System.out.println(id);
        List<JobEvent> result = this.jobEvents.stream()
                .filter(j -> id.equals(j.getProduct()))
                .collect(Collectors.toList());
        Collections.sort(result);
        result.forEach(l -> println(l));
    }

    @Override
    public void printlnSimpleOpEvents() {
        this.opEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getOperation()))
                .forEach((k, v) -> {
                    Collections.sort(v, this::sort);
                    System.out.println(k);
                    v.forEach(l -> printlnSimple(l));
                });
    }

    @Override
    public void printlnSimpleEquipEvents() {
        this.equipEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getEquip()))
                .forEach((k, v) -> {
                    Collections.sort(v, this::sort);
                    System.out.println(k);
                    v.forEach(l -> printlnSimple(l));
                });
    }

    @Override
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

    @Override
    public void log(OpEvent e) {
        this.opEvents.add(e);
    }

    @Override
    public void log(EquipEvent e) {
        this.equipEvents.add(e);
    }

    @Override
    public void log(JobEvent e) {
        this.jobEvents.add(e);
    }

    public String toTimeString(int time) {
        return this.factory.getTimeType().format(time);
    }

    @Override
    public String flush(String name) {
        return name;
    }

    @Override
    public List<OpEvent> getOpEvents() {
        return this.opEvents;
    }

    @Override
    public List<EquipEvent> getEquipEvents() {
        return this.equipEvents;
    }

    @Override
    public List<JobEvent> getJobEvents() {
        return this.jobEvents;
    }

    private void println(Event l) {
        System.out.println(String.format("%8s %8d %-15s - %s",
                this.factory.getTimeType().format(l.getTime()),
                l.getTime(),
                l.getEvent(),
                this.gson.toJson(l)));
    }

    private void printlnSimple(Event l) {
        System.out.println(String.format("%8s %8s",
                this.factory.getTimeType().format(l.getTime()),
                l));
    }

    private int sort(Event e1, Event e2) {
        return e1.getTime() - e2.getTime();
    }

    @Override
    public void setEndTime(Date endTime) {
    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public void setTotal(int total) {
    }

    @Override
    public int getTotal() {
        return 0;
    }
}
