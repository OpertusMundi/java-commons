package eu.opertusmundi.common.model.catalogue.elastic;

import lombok.Getter;

public enum EnumElasticSearchSortField {
    TITLE("properties.title.keyword"),
    PUBLICATION_DATE("properties.publication_date"),
    SCORE("_score")
    ;

    @Getter
    private String value;

    private EnumElasticSearchSortField(String value) {
        this.value = value;
    }

    public static EnumElasticSearchSortField fromValue(String value) {
        for (final EnumElasticSearchSortField e : EnumElasticSearchSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumElasticSearchSortField]", value));
    }

}