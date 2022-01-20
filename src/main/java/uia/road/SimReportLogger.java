package uia.road;

import java.util.Date;
import java.util.List;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;

public interface SimReportLogger {

    public void printlnOpEvents(boolean group);

    public void printlnOpEvents(String id);

    public void printlnEquipEvents(boolean group);

    public void printlnEquipEvents(String id);

    public void printlnJobEvents(boolean group);

    public void printlnJobEvents(String id);

    public void printlnSimpleOpEvents();

    public void printlnSimpleEquipEvents();

    public void printlnSimpleJobEvents();

    public void log(OpEvent e);

    public void log(EquipEvent e);

    public void log(JobEvent e);

    public List<OpEvent> getOpEvents();

    public List<EquipEvent> getEquipEvents();

    public List<JobEvent> getJobEvents();

    public String flush(String name);

    public void setEndTime(Date endTime);

    public Date getEndTime();

    public void setTotal(int total);

    public int getTotal();
}
