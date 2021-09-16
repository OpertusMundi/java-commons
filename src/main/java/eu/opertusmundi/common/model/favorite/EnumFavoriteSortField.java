package eu.opertusmundi.common.model.favorite;

import lombok.Getter;

public enum EnumFavoriteSortField {
    CREATED_ON("createdOn"),
    TITLE("title"),
    ;

    @Getter
    private String value;

    private EnumFavoriteSortField(String value) {
        this.value = value;
    }

    public static EnumFavoriteSortField fromValue(String value) {
        for (final EnumFavoriteSortField e : EnumFavoriteSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumFavoriteSortField]", value));
    }

}