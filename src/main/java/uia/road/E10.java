package uia.road;

/**
 * 
 * 
 * https://www.semi.org/en/Standards/CTR_031244
 * 
 * @author Kan
 *
 */
public class E10 {

    private int nonScheduledTime;

    private int unscheduledDownTime;

    private int scheduledDownTime;

    private int engineeringTime;

    private int standbyTime;

    private int productiveTime;

    public int getNonScheduledTime() {
        return this.nonScheduledTime;
    }

    public void setNonScheduledTime(int nonScheduledTime) {
        this.nonScheduledTime = nonScheduledTime;
    }

    public int getUnscheduledDownTime() {
        return this.unscheduledDownTime;
    }

    public void setUnscheduledDownTime(int unscheduledDownTime) {
        this.unscheduledDownTime = unscheduledDownTime;
    }

    public int getScheduledDownTime() {
        return this.scheduledDownTime;
    }

    public void setScheduledDownTime(int scheduledDownTime) {
        this.scheduledDownTime = scheduledDownTime;
    }

    public int getEngineeringTime() {
        return this.engineeringTime;
    }

    public void setEngineeringTime(int engineeringTime) {
        this.engineeringTime = engineeringTime;
    }

    public int getStandbyTime() {
        return this.standbyTime;
    }

    public void setStandbyTime(int standbyTime) {
        this.standbyTime = standbyTime;
    }

    public int getProductiveTime() {
        return this.productiveTime;
    }

    public void setProductiveTime(int productiveTime) {
        this.productiveTime = productiveTime;
    }

    public int getEquipmentDownTime() {
        return this.unscheduledDownTime + this.scheduledDownTime;
    }

    public int getEquipmentUpTime() {
        return this.engineeringTime + this.standbyTime + this.productiveTime;
    }

    public int getManufacturingTime() {
        return this.standbyTime + this.productiveTime;
    }

    public int getOperationTime() {
        return getEquipmentDownTime() + getEquipmentUpTime();
    }

    public void addNonScheduledTime(int nonScheduledTime) {
        this.nonScheduledTime += nonScheduledTime;
    }

    public void addUnscheduledDownTime(int unscheduledDownTime) {
        this.unscheduledDownTime += unscheduledDownTime;
    }

    public void addScheduledDownTime(int scheduledDownTime) {
        this.scheduledDownTime += scheduledDownTime;
    }

    public void addEngineeringTime(int engineeringTime) {
        this.engineeringTime += engineeringTime;
    }

    public void addStandnbyTime(int standbyTime) {
        this.standbyTime += standbyTime;
    }

    public void addProductiveTime(int productiveTime) {
        this.productiveTime += productiveTime;
    }
}
