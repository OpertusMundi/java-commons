package eu.opertusmundi.common.model.dto;

public enum EnumCustomerType {
    UNDEFINED(0),
    INDIVIDUAL(1),
    PROFESSIONAL(2),
    ;

    private int value;

    EnumCustomerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumCustomerType fromValue(Integer value) {
        if (value == null) {
            return null;
        }

        for (final EnumCustomerType t : EnumCustomerType.values()) {
            if (t.value == value) {
                return t;
            }
        }

        return null;
    }

}