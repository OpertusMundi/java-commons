package eu.opertusmundi.common.repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.ServiceBillingBatchEntity;
import eu.opertusmundi.common.model.payment.BillingSubscriptionDates;
import eu.opertusmundi.common.model.payment.EnumServiceBillingBatchStatus;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchDto;

@Repository
@Transactional(readOnly = true)
public interface ServiceBillingBatchRepository extends JpaRepository<ServiceBillingBatchEntity, Integer> {

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
    Optional<HelpdeskAccountEntity> findAccountById(Integer id);

    @Query("SELECT  b "
         + "FROM    ServiceBillingBatch b "
         + "WHERE   (b.status in :status or :status is null) "
    )
    Page<ServiceBillingBatchEntity> findAllEntities(Set<EnumServiceBillingBatchStatus> status, Pageable pageable);

    default Page<ServiceBillingBatchDto> findAllObjects(Set<EnumServiceBillingBatchStatus> status, Pageable pageable) {
        final Page<ServiceBillingBatchEntity> page = this.findAllEntities(
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return page.map(ServiceBillingBatchEntity::toDto);
    }

    @Query("SELECT  b "
         + "FROM    ServiceBillingBatch b "
         + "WHERE   (b.fromDate = :fromDate) and "
         + "        (b.toDate = :toDate) "
    )
    Optional<ServiceBillingBatchEntity> findOneByInterval(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT  b "
         + "FROM    ServiceBillingBatch b "
         + "WHERE   (b.key = :key) "
    )
    Optional<ServiceBillingBatchEntity> findOneByKey(UUID key);

    default Optional<ServiceBillingBatchDto> findOneObjectByKey(UUID key) {
        return this.findOneByKey(key).map(ServiceBillingBatchEntity::toDto);
    }

    @Transactional(readOnly = false)
    default ServiceBillingBatchEntity findOneOrCreate(ServiceBillingBatchCommandDto command, BillingSubscriptionDates dates) {
        final ServiceBillingBatchEntity e = this.findOneByInterval(dates.getDateFrom(), dates.getDateTo()).orElse(null);

        if (e != null) {
            e.setUpdatedOn(ZonedDateTime.now());
            e.setStatus(EnumServiceBillingBatchStatus.RUNNING);
            e.setProcessInstance(null);
            e.setProcessDefinition(null);
        }

        return e == null ? this.create(command, dates) : e;
    }

    @Transactional(readOnly = false)
    default ServiceBillingBatchEntity create(ServiceBillingBatchCommandDto command, BillingSubscriptionDates dates) {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(dates, "Expected a non-null dates object");

        final ZonedDateTime         now       = ZonedDateTime.now();
        final HelpdeskAccountEntity createdBy = this.findAccountById(command.getUserId()).orElse(null);

        final ServiceBillingBatchEntity e = ServiceBillingBatchEntity.builder()
            .createdBy(createdBy)
            .createdOn(now)
            .dueDate(dates.getDateDue())
            .fromDate(dates.getDateFrom())
            .toDate(dates.getDateTo())
            .updatedOn(now)
            .build();

        return this.saveAndFlush(e);
    }
    
    @Transactional(readOnly = false)
    default void setStatus(UUID key, EnumServiceBillingBatchStatus status) {
        final ServiceBillingBatchEntity e = this.findOneByKey(key).orElse(null);

        e.setStatus(status);
        e.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(e);
    }
}
