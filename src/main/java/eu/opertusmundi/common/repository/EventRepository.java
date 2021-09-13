package eu.opertusmundi.common.repository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.EventEntity;
import eu.opertusmundi.common.model.logging.EnumEventLevel;

@Repository
@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<EventEntity, Integer> {

    @Query("SELECT e FROM Event e WHERE "
         + "(e.level in :level or :level is null) and "
         + "(e.logger like :logger or :logger is null) and "
         + "(e.userName like :userName or :userName is null) and "
         + "(e.clientAddress like :clientAddress or :clientAddress is null) "
     )
    Page<EventEntity> findAll(
        @Param("level") Set<EnumEventLevel> level,
        @Param("logger") String logger,
        @Param("userName") String userName,
        @Param("clientAddress") String clientAddress,
        Pageable pageable
    );

}
