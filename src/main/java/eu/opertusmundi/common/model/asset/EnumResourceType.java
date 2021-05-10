package eu.opertusmundi.common.model.asset;

public enum EnumResourceType {
    FILE,
    SERVICE,
    ;

    public static EnumResourceType fromString(String value) {
        for (final EnumResourceType item : EnumResourceType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
