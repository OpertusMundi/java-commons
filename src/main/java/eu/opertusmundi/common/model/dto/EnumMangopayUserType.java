package eu.opertusmundi.common.model.dto;

public enum EnumMangopayUserType {
    UNDEFINED(0),
    INDIVIDUAL(1),
    PROFESSIONAL(2),
    ;

    private int value;

    EnumMangopayUserType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumMangopayUserType fromValue(Integer value) {
        if (value == null) {
            return null;
        }

        for (final EnumMangopayUserType t : EnumMangopayUserType.values()) {
            if (t.value == value) {
                return t;
            }
        }

        return null;
    }

}