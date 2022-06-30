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
import eu.opertusmundi.common.domain.SubscriptionBillingBatchEntity;
import eu.opertusmundi.common.model.payment.BillingSubscriptionDates;
import eu.opertusmundi.common.model.payment.EnumSubscriptionBillingBatchStatus;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchDto;

@Repository
@Transactional(readOnly = true)
public interface SubscriptionBillingBatchRepository extends JpaRepository<SubscriptionBillingBatchEntity, Integer> {

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
    Optional<HelpdeskAccountEntity> findAccountById(Integer id);

    @Query("SELECT  b "
         + "FROM    SubscriptionBillingBatch b "
         + "WHERE   (b.status in :status or :status is null) "
    )
    Page<SubscriptionBillingBatchEntity> findAllEntities(Set<EnumSubscriptionBillingBatchStatus> status, Pageable pageable);

    default Page<SubscriptionBillingBatchDto> findAllObjects(Set<EnumSubscriptionBillingBatchStatus> status, Pageable pageable) {
        final Page<SubscriptionBillingBatchEntity> page = this.findAllEntities(
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return page.map(SubscriptionBillingBatchEntity::toDto);
    }

    @Query("SELECT  b "
         + "FROM    SubscriptionBillingBatch b "
         + "WHERE   (b.fromDate = :fromDate) and "
         + "        (b.toDate = :toDate) "
    )
    Optional<SubscriptionBillingBatchEntity> findOneByInterval(LocalDate fromDate, LocalDate toDate);

    @Query("SELECT  b "
         + "FROM    SubscriptionBillingBatch b "
         + "WHERE   (b.key = :key) "
    )
    Optional<SubscriptionBillingBatchEntity> findOneByKey(UUID key);

    default Optional<SubscriptionBillingBatchDto> findOneObjectByKey(UUID key) {
        return this.findOneByKey(key).map(SubscriptionBillingBatchEntity::toDto);
    }

    @Transactional(readOnly = false)
    default SubscriptionBillingBatchEntity findOneOrCreate(SubscriptionBillingBatchCommandDto command, BillingSubscriptionDates dates) {
        final Optional<SubscriptionBillingBatchEntity> e = this.findOneByInterval(dates.getDateFrom(), dates.getDateTo());

        return e.isPresent() ? e.get() : this.create(command, dates);
    }

    @Transactional(readOnly = false)
    default SubscriptionBillingBatchEntity create(SubscriptionBillingBatchCommandDto command, BillingSubscriptionDates dates) {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(dates, "Expected a non-null dates object");

        final ZonedDateTime         now       = ZonedDateTime.now();
        final HelpdeskAccountEntity createdBy = this.findAccountById(command.getUserId()).orElse(null);

        final SubscriptionBillingBatchEntity e = SubscriptionBillingBatchEntity.builder()
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
    default void setStatus(UUID key, EnumSubscriptionBillingBatchStatus status) {
        final SubscriptionBillingBatchEntity e = this.findOneByKey(key).orElse(null);

        e.setStatus(status);
        e.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(e);
    }
}
