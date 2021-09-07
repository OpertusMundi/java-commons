package eu.opertusmundi.common.model.catalogue.elastic;

import lombok.Getter;

public enum EnumElasticSearchDatasetSize {
	SMALL(null, 1000),
	MEDIUM(1000, 100000),
	LARGE(100000, null),
	;

    @Getter
    private Integer min;
    @Getter
    private Integer max;

    EnumElasticSearchDatasetSize(Integer min, Integer max) {
        this.min = min;
        this.max = max;
	}

}
