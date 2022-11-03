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

import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;

@Repository
@Transactional(readOnly = true)
public interface SubscriptionBillingRepository extends JpaRepository<SubscriptionBillingEntity, Integer> {

    @Query("SELECT  b "
         + "FROM    SubscriptionBilling b "
         + "WHERE   (b.status in :status or :status is null) and "
         + "        (cast(:consumerKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.consumer.key = :consumerKey) and "
         + "        (cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.provider.key = :providerKey) and "
         + "        (cast(:subscriptionKey as org.hibernate.type.UUIDCharType) IS NULL or b.subscription.key = :subscriptionKey) "
    )
    Page<SubscriptionBillingEntity> findAllEntities(
        UUID consumerKey, UUID providerKey, UUID subscriptionKey, Set<EnumSubscriptionBillingStatus> status, Pageable pageable
    );

    default Page<SubscriptionBillingDto> findAllObjects(
        EnumView view, boolean includeDetails,
        UUID consumerKey, UUID providerKey, UUID subscriptionKey, Set<EnumSubscriptionBillingStatus> status, Pageable pageable
    ) {
        final Page<SubscriptionBillingEntity> page = this.findAllEntities(
            consumerKey,
            providerKey,
            subscriptionKey,
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return switch (view) {
            case CONSUMER -> page.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> page.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> page.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    @Query("SELECT b FROM SubscriptionBilling b WHERE (b.key in :keys)")
    List<SubscriptionBillingEntity> findAllEntitiesByKeys(List<UUID> keys);

    default List<SubscriptionBillingDto> findAllObjectsByKeys(EnumView view, List<UUID> keys, boolean includeDetails) {
        final List<SubscriptionBillingEntity> records = this.findAllEntitiesByKeys(keys);

        return switch (view) {
            case CONSUMER -> records.stream().map(s -> s.toConsumerDto(includeDetails)).collect(Collectors.toList());
            case PROVIDER -> records.stream().map(s -> s.toProviderDto(includeDetails)).collect(Collectors.toList());
            case HELPDESK -> records.stream().map(s -> s.toHelpdeskDto(includeDetails)).collect(Collectors.toList());
        };
    }

    @Query("SELECT b FROM SubscriptionBilling b WHERE (b.key = :key)")
    Optional<SubscriptionBillingEntity> findOneEntity(UUID key);

    default Optional<SubscriptionBillingDto> findOneObject(EnumView view, UUID key, boolean includeDetails) {
        final Optional<SubscriptionBillingEntity> e = this.findOneEntity(key);

        return switch (view) {
            case CONSUMER -> e.map(s -> s.toConsumerDto(includeDetails));
            case PROVIDER -> e.map(s -> s.toProviderDto(includeDetails));
            case HELPDESK -> e.map(s -> s.toHelpdeskDto(includeDetails));
        };
    }

    @Query("SELECT  b "
         + "FROM    SubscriptionBilling b "
         + "WHERE   (b.fromDate = :fromDate) and "
         + "        (b.toDate = :toDate) and "
         + "        (b.subscription.id = :subscriptionId) "
    )
    Optional<SubscriptionBillingEntity> findOneBySubscriptionAndInterval(
        LocalDate fromDate, LocalDate toDate, Integer subscriptionId
    );
}
