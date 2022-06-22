package eu.opertusmundi.common.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
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
         + "        (:subscriptionId IS NULL or b.subscription.id = :subscriptionId) "
    )
    Page<SubscriptionBillingEntity> findAllEntitiesByConsumer(
        UUID consumerKey, UUID providerKey, Integer subscriptionId, Set<EnumSubscriptionBillingStatus> status, Pageable pageable
    );

    default Page<SubscriptionBillingDto> findAllObjectsByConsumer(
        UUID consumerKey, UUID providerKey, Integer subscriptionId, Set<EnumSubscriptionBillingStatus> status, Pageable pageable
    ) {
        final Page<SubscriptionBillingEntity> page = this.findAllEntitiesByConsumer(
            consumerKey,
            providerKey,
            subscriptionId,
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return page.map(s-> s.toHelpdeskDto(true));
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
