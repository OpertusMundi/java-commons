package eu.opertusmundi.common.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import eu.opertusmundi.common.domain.ServiceUsageSummaryEntity;
import eu.opertusmundi.common.model.account.ServiceUsageKey;

@Repository
@Transactional(readOnly = true)
public interface ServiceUsageSummaryRepository extends JpaRepository<ServiceUsageSummaryEntity, ServiceUsageKey>
{
    @Query("SELECT s FROM ServiceUsageSummary s WHERE s.serviceKey = :serviceKey")
    List<ServiceUsageSummaryEntity> findAllByServiceKey(@Param("serviceKey") UUID serviceKey);
    
    default Optional<ServiceUsageSummaryEntity> findOneByServiceKeyAndMonthOfYear(UUID serviceKey, int year, int month)
    {
        return this.findById(ServiceUsageKey.of(serviceKey, LocalDate.of(year, month, 1)));
    }
}
