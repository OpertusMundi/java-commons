package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;
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
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;

@Repository
@Transactional(readOnly = true)
public interface AccountSubscriptionRepository extends JpaRepository<AccountSubscriptionEntity, Integer> {

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    List<AccountSubscriptionEntity> findAllEntitiesByConsumer(UUID userKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    default List<AccountSubscriptionDto> findAllObjectsByConsumer(UUID userKey, boolean includeProviderDetails) {
        return this.findAllEntitiesByConsumer(userKey).stream()
            .map(e -> e.toConsumerDto(includeProviderDetails))
            .collect(Collectors.toList());
    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and order.key = :orderKey")
    Optional<AccountSubscriptionEntity> findAllEntitiesByConsumerAndOrder(UUID userKey, UUID orderKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and order.key = :orderKey")
    default Optional<AccountSubscriptionDto> findAllObjectsByConsumerAndOrder(UUID userKey, UUID orderKey, boolean includeProviderDetails) {
        return this.findAllEntitiesByConsumerAndOrder(userKey, orderKey)
            .map(e -> e.toConsumerDto(includeProviderDetails));

    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.asset = :assetId")
    List<AccountSubscriptionEntity> findAllByConsumerAndServiceId(UUID userKey, String assetId);


    @Query("SELECT s FROM AccountSubscription s WHERE s.provider.key = :userKey")
    Page<AccountSubscriptionEntity> findAllEntitiesByProvider(UUID userKey, Pageable pageable);

    @Query("SELECT s FROM AccountSubscription s WHERE s.provider.key = :userKey")
    default Page<ProviderAccountSubscriptionDto> findAllObjectsByProvider(UUID userKey, Pageable pageable) {
        return this.findAllEntitiesByProvider(userKey, pageable).map(e -> e.toProviderDto());
    }

}
