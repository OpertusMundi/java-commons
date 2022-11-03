package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
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

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.EnumSubscriptionStatus;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;

@Repository
@Transactional(readOnly = true)
public interface AccountSubscriptionRepository extends JpaRepository<AccountSubscriptionEntity, Integer> {

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    List<AccountSubscriptionEntity> findAllEntitiesByConsumer(UUID userKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.status = :status")
    List<AccountSubscriptionEntity> findAllEntitiesByConsumerAndStatus(UUID userKey, EnumSubscriptionStatus status);

    @Query("SELECT  s "
         + "FROM    AccountSubscription s "
         + "WHERE   (s.status in :status or :status is null) and "
         + "        (cast(:consumerKey as org.hibernate.type.UUIDCharType) IS NULL or s.consumer.key = :consumerKey) and "
         + "        (cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL or s.provider.key = :providerKey) "
     )
    Page<AccountSubscriptionEntity> findAllEntitiesByConsumer(
        UUID consumerKey, UUID providerKey, Set<EnumSubscriptionStatus> status, Pageable pageable
    );

    default List<AccountSubscriptionDto> findAllObjectsByConsumer(UUID userKey, boolean includeProviderDetails) {
        return this.findAllEntitiesByConsumer(userKey).stream()
            .map(e -> e.toConsumerDto(includeProviderDetails))
            .collect(Collectors.toList());
    }

    default Page<AccountSubscriptionDto> findAllObjectsByConsumer(
        UUID consumerKey, UUID providerKey, Set<EnumSubscriptionStatus> status, Pageable pageable
    ) {
        final Page<AccountSubscriptionEntity> page = this.findAllEntitiesByConsumer(
            consumerKey,
            providerKey,
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return page.map(AccountSubscriptionEntity::toHelpdeskDto);
    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.key = :subscriptionKey")
    Optional<AccountSubscriptionEntity> findOneByConsumerAndOrder(UUID userKey, UUID subscriptionKey);

    default Optional<AccountSubscriptionDto> findOneObjectByConsumerAndOrder(
        UUID userKey, UUID subscriptionKey, boolean includeProviderDetails
    ) {
        return this.findOneByConsumerAndOrder(userKey, subscriptionKey)
            .map(e -> e.toConsumerDto(includeProviderDetails));

    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.assetId = :assetId")
    List<AccountSubscriptionEntity> findAllByConsumerAndAssetId(UUID userKey, String assetId);

    @Query("SELECT s FROM AccountSubscription s WHERE s.provider.key = :userKey")
    Page<AccountSubscriptionEntity> findAllEntitiesByProvider(UUID userKey, Pageable pageable);

    default Page<ProviderAccountSubscriptionDto> findAllObjectsByProvider(UUID userKey, Pageable pageable) {
        return this.findAllEntitiesByProvider(userKey, pageable).map(e -> e.toProviderDto());
    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :consumerKey and s.provider.key = :providerKey")
    List<AccountSubscriptionEntity> findAllByConsumerAndProvider(UUID consumerKey, UUID providerKey);

    default boolean subscriptionExists(UUID userKey, String assetId, boolean active) {
        final List<AccountSubscriptionEntity> subs      = this.findAllByConsumerAndAssetId(userKey, assetId);
        final AccountSubscriptionEntity       activeSub = subs.stream()
            .filter(s -> s.getExpiresOn() == null || s.getExpiresOn().isAfter(ZonedDateTime.now()))
            .findFirst()
            .orElse(null);

        return active ? activeSub != null : !subs.isEmpty();
    }

    default boolean providerSubscriptionExists(UUID userKey, UUID providerKey, boolean active) {
        final List<AccountSubscriptionEntity> subs      = this.findAllByConsumerAndProvider(userKey, providerKey);
        final AccountSubscriptionEntity       activeSub = subs.stream()
            .filter(s -> s.getExpiresOn() == null || s.getExpiresOn().isAfter(ZonedDateTime.now()))
            .findFirst()
            .orElse(null);

        return active ? activeSub != null : !subs.isEmpty();
    }

}
