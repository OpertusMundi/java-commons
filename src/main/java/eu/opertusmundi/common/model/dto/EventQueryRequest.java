package eu.opertusmundi.common.model.dto;

import org.springframework.data.domain.PageRequest;

import eu.opertusmundi.common.model.PageRequestDto;

public class EventQueryRequest {

    private PageRequestDto pagingOptions;

    private EventQuery query;

    public PageRequestDto getPagingOptions() {
        return this.pagingOptions;
    }

    public PageRequest getPageRequest() {
        return this.pagingOptions == null ? null : PageRequest.of(this.pagingOptions.getPage(), this.pagingOptions.getSize());
    }

    public void setPagingOptions(PageRequestDto pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public EventQuery getQuery() {
        return this.query;
    }

    public void setQuery(EventQuery query) {
        this.query = query;
    }

}
