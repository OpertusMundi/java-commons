package eu.opertusmundi.common.repository;

import org.springframework.data.domain.PageRequest;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.dto.EventDto;
import eu.opertusmundi.common.model.dto.EventQuery;

public interface EventRepository {

    /**
     * Find system events filtered by a {@link EventQuery}
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     */
	PageResultDto<EventDto> query(EventQuery query, PageRequest pageReq);

}
