package uia.road;

import java.math.BigDecimal;

public class E79 {

    private BigDecimal totalTime;

    private E10 e10;

    public E79(int totalTime, E10 e10) {
        this.totalTime = BigDecimal.valueOf(totalTime);
        this.e10 = e10;
    }

    public BigDecimal availabilityEfficiency() {
        return BigDecimal
                .valueOf(this.e10.getEquipmentUpTime() * 100)
                .divide(this.totalTime, 2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal operationEfficiency() {
        return BigDecimal
                .valueOf(this.e10.getProductiveTime() * 100)
                .divide(BigDecimal.valueOf(this.e10.getEquipmentUpTime()), 2, BigDecimal.ROUND_HALF_UP);
    }
}
