package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumRecurringPaymentFrequency {
    MONTHLY("Monthly"),
    ANNUAL("Annual"),
    ;

    @Getter
    private String value;

    private EnumRecurringPaymentFrequency(String value) {
        this.value = value;
    }

}
