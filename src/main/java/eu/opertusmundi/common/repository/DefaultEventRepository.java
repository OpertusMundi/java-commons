package eu.opertusmundi.common.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.EventEntity;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.dto.EventDto;
import eu.opertusmundi.common.model.dto.EventQuery;

@Repository()
@Transactional(readOnly = true)
public class DefaultEventRepository implements EventRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    public PageResultDto<EventDto> query(EventQuery query, PageRequest pageReq) {
        // Check query parameters
        if (pageReq == null) {
            pageReq = PageRequest.of(0, 10);
        }

        String qlString = "";

        // Resolve filters

        final List<String> filters = new ArrayList<>();
        if (query != null) {
            if (query.getMinDate() != null) {
                filters.add("(e.generated >= :minDate)");
            }
            if (query.getMaxDate() != null) {
                filters.add("(e.generated <= :maxDate)");
            }
            if (query.getLevel() != null) {
                filters.add("(e.level = :level)");
            }
            if (!StringUtils.isEmpty(query.getUserName())) {
                filters.add("(e.userName like :userName)");
            }
            if (!StringUtils.isEmpty(query.getSource())) {
                filters.add("(e.clientAddress like :clientAddress)");
            }
        }

        // Count records
        qlString = "select count(e.id) from Event e ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }

        Integer count;
        final TypedQuery<Number> countQuery = this.entityManager.createQuery(qlString, Number.class);
        if (query != null) {
            this.setFindParameters(query, countQuery);
        }
        count = countQuery.getSingleResult().intValue();

        // Load records
        qlString = "select e from Event e ";
        if (!filters.isEmpty()) {
            qlString += " where " + StringUtils.join(filters, " and ");
        }
        qlString += " order by e.generated desc, e.id desc ";

        final TypedQuery<EventEntity> selectQuery = this.entityManager.createQuery(qlString, EventEntity.class);
        if (query != null) {
            this.setFindParameters(query, selectQuery);
        }

		selectQuery.setFirstResult((int) pageReq.getOffset());
        selectQuery.setMaxResults(pageReq.getPageSize());

        final List<EventDto> records = selectQuery.getResultList().stream()
            .map(EventEntity::toDto)
            .collect(Collectors.toList());

        return PageResultDto.of(pageReq.getPageNumber(), pageReq.getPageSize(), records, count);
    }

    private void setFindParameters(EventQuery eventQuery, Query query) {
        if (eventQuery.getMinDate() != null) {
            query.setParameter("minDate", eventQuery.getMinDate());
        }
        if (eventQuery.getMaxDate() != null) {
            query.setParameter("maxDate", eventQuery.getMaxDate());
        }
        if (eventQuery.getLevel() != null) {
            query.setParameter("level", eventQuery.getLevel());
        }
        if (!StringUtils.isEmpty(eventQuery.getUserName())) {
            query.setParameter("userName", "%" + eventQuery.getUserName() + "%");
        }
        if (!StringUtils.isEmpty(eventQuery.getSource())) {
            query.setParameter("clientAddress", "%" + eventQuery.getSource() + "%");
        }
    }

}
