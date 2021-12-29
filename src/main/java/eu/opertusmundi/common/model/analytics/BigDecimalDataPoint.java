package eu.opertusmundi.common.model.analytics;

import java.math.BigDecimal;

public class BigDecimalDataPoint extends DataPoint<BigDecimal> {

    public BigDecimalDataPoint(BigDecimal value) {
        this.setValue(value);
    }

    public BigDecimalDataPoint(Integer year, Integer month, Integer week, Integer day, BigDecimal value) {
        this.setValue(value);
        this.setTime(new TimeInstant(year, month, week, day));
    }

    public BigDecimalDataPoint(Integer year, Integer month, Integer week, BigDecimal value) {
        this(year, month, week, null, value);
    }

    public BigDecimalDataPoint(Integer year, Integer month, BigDecimal value) {
        this(year, month, null, null, value);
    }

    public BigDecimalDataPoint(Integer year, BigDecimal value) {
        this(year, null, null, null, value);
    }

}
