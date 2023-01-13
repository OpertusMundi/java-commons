package eu.opertusmundi.common.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.ServiceBillingEntity;
import eu.opertusmundi.common.domain.TransferEntity;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;

@Repository
@Transactional(readOnly = true)
public interface ServiceBillingRepository extends JpaRepository<ServiceBillingEntity, Integer> {

    @Query("""
        SELECT  r
        FROM    ServiceBilling r
                    LEFT OUTER JOIN r.userService s
                    LEFT OUTER JOIN s.account s_owner
                    LEFT OUTER JOIN r.subscription sub
                    LEFT OUTER JOIN sub.consumer sub_consumer
                    LEFT OUTER JOIN sub.provider sub_provider
        WHERE   (r.type = :type or :type is null) and
                (r.status in :status or :status is null) and
                (cast(:ownerKey as org.hibernate.type.UUIDCharType) IS NULL or sub_consumer.key = :ownerKey or s_owner.key = :ownerKey) and
                (cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL or sub_provider.key = :providerKey) and
                (cast(:serviceKey as org.hibernate.type.UUIDCharType)  IS NULL or sub.key = :serviceKey or s.key = :serviceKey)
    """)
    Page<ServiceBillingEntity> findAll(
        EnumBillableServiceType type,
        UUID ownerKey, UUID providerKey, UUID serviceKey, Set<EnumPayoffStatus> status,
        Pageable pageable
    );

    default Page<ServiceBillingDto> findAllObjects(
        EnumView view, EnumBillableServiceType type,
        UUID ownerKey, UUID providerKey, UUID serviceKey, Set<EnumPayoffStatus> status,
        Pageable pageable, boolean includeDetails
    ) {
        final Page<ServiceBillingEntity> page = this.findAll(
            type, ownerKey, providerKey, serviceKey, status != null && status.size() > 0 ? status : null, pageable
        );

        return switch (view) {
            case CONSUMER -> page.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> page.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> page.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    @Query("SELECT b FROM ServiceBilling b WHERE (b.key in :keys)")
    List<ServiceBillingEntity> findAllByKey(List<UUID> keys);

    default List<ServiceBillingDto> findAllObjectsByKey(EnumView view, List<UUID> keys, boolean includeDetails) {
        final List<ServiceBillingEntity> records = this.findAllByKey(keys);

        return switch (view) {
            case CONSUMER -> records.stream().map(s -> s.toConsumerDto(includeDetails)).collect(Collectors.toList());
            case PROVIDER -> records.stream().map(s -> s.toProviderDto(includeDetails)).collect(Collectors.toList());
            case HELPDESK -> records.stream().map(s -> s.toHelpdeskDto(includeDetails)).collect(Collectors.toList());
        };
    }

    @Query("""
        SELECT  b
        FROM    ServiceBilling b
        WHERE   (b.type = 'SUBSCRIPTION') and
                (b.status in :status or :status is null) and
                (cast(:consumerKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.consumer.key = :consumerKey) and
                (cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.provider.key = :providerKey) and
                (cast(:subscriptionKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.key = :subscriptionKey)
    """)
    Page<ServiceBillingEntity> findAllSubscriptions(
        UUID consumerKey, UUID providerKey, UUID subscriptionKey, Set<EnumPayoffStatus> status, Pageable pageable
    );

    default Page<ServiceBillingDto> findAllSubscriptionObjects(
        EnumView view, boolean includeDetails, UUID consumerKey, UUID providerKey, UUID subscriptionKey, Set<EnumPayoffStatus> status, Pageable pageable
    ) {
        final Page<ServiceBillingEntity> page = this.findAllSubscriptions(
            consumerKey, providerKey, subscriptionKey, status != null && status.size() > 0 ? status : null, pageable
        );

        return switch (view) {
            case CONSUMER -> page.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> page.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> page.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    @Query("""
        SELECT  b
        FROM    ServiceBilling b
        WHERE   (b.fromDate = :fromDate) and
                (b.toDate = :toDate) and
                (b.subscription.id = :subscriptionId)
    """)
    Optional<ServiceBillingEntity> findOneBySubscriptionIdAndInterval(LocalDate fromDate, LocalDate toDate, Integer subscriptionId);

    @Query("""
        SELECT  b
        FROM    ServiceBilling b
        WHERE   (b.type = 'PRIVATE_OGC_SERVICE') and
                (b.status in :status or :status is null) and
                (cast(:ownerKey as org.hibernate.type.UUIDCharType) IS NULL or b.userService.account.key = :ownerKey) and
                (cast(:ownerParentKey as org.hibernate.type.UUIDCharType) IS NULL or b.userService.account.parent.key = :ownerParentKey) and
                (cast(:serviceKey as org.hibernate.type.UUIDCharType) IS NULL or b.userService.key = :serviceKey)
    """)
    Page<ServiceBillingEntity> findAllUserServices(
        UUID ownerKey, UUID ownerParentKey, UUID serviceKey, Set<EnumPayoffStatus> status, Pageable pageable
    );

    default Page<ServiceBillingDto> findAllUserServiceObjects(
        EnumView view, boolean includeDetails, UUID ownerKey, UUID ownerParentKey, UUID serviceKey, Set<EnumPayoffStatus> status, Pageable pageable
    ) {
        final Page<ServiceBillingEntity> page = this.findAllUserServices(
            ownerKey, ownerParentKey, serviceKey, status != null && status.size() > 0 ? status : null, pageable
        );

        return switch (view) {
            case CONSUMER -> page.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> page.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> page.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    @Query("""
        SELECT  b
        FROM    ServiceBilling b
        WHERE   (b.fromDate = :fromDate) and
                (b.toDate = :toDate) and
                (b.userService.id = :serviceId)
    """)
    Optional<ServiceBillingEntity> findOneByUserServiceIdAndInterval(LocalDate fromDate, LocalDate toDate, Integer serviceId);

    @Query("SELECT b FROM ServiceBilling b WHERE (b.key = :key)")
    Optional<ServiceBillingEntity> findOneByKey(UUID key);

    default Optional<ServiceBillingDto> findOneSubscriptionObjectByKey(EnumView view, UUID key, boolean includeDetails) {
        final Optional<ServiceBillingEntity> e = this.findOneByKey(key);

        return switch (view) {
            case CONSUMER -> e.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> e.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> e.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    default void updateTransfer(Integer id, TransferEntity transfer) {
        Assert.notNull(transfer, "Expected a non-null transfer");

        final ServiceBillingEntity e = this.findById(id).orElse(null);

        Assert.notNull(e, "Expected a non-null service billing record");

        e.updateTransfer(transfer);

        this.saveAndFlush(e);
    }
}
