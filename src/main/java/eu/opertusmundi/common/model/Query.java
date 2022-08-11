package eu.opertusmundi.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Generic query with data pagination support
 */
@Getter
@Setter
public class Query {

    private PageRequestDto pagingOptions;

    public PageRequestDto getPagingOptions() {
        return this.pagingOptions;
    }

    public void setPagingOptions(PageRequestDto pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

}
