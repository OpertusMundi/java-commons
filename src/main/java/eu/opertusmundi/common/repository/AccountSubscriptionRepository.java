package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;

@Repository
@Transactional(readOnly = true)
public interface AccountSubscriptionRepository extends JpaRepository<AccountSubscriptionEntity, Integer> {

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    List<AccountSubscriptionEntity> findAllEntitiesByConsumer(@Param("userKey") UUID userKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    default List<AccountSubscriptionDto> findAllObjectsByConsumer(@Param("userKey") UUID userKey) {
        return this.findAllEntitiesByConsumer(userKey).stream()
            .map(e -> e.toConsumerDto())
            .collect(Collectors.toList());
    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and order.key = :orderKey")
    Optional<AccountSubscriptionEntity> findAllEntitiesByConsumerAndOrder(UUID userKey, UUID orderKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and order.key = :orderKey")
    default Optional<AccountSubscriptionDto> findAllObjectsByConsumerAndOrder(UUID userKey, UUID orderKey) {
        return this.findAllEntitiesByConsumerAndOrder(userKey, orderKey)
            .map(e -> e.toConsumerDto());

    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.service = :assetId")
    List<AccountSubscriptionEntity> findAllByConsumerAndServiceId(@Param("userKey") UUID userKey, @Param("assetId") String assetId);


    @Query("SELECT s FROM AccountSubscription s WHERE s.provider.key = :userKey")
    Page<AccountSubscriptionEntity> findAllEntitiesByProvider(@Param("userKey") UUID userKey, Pageable pageable);

    @Query("SELECT s FROM AccountSubscription s WHERE s.provider.key = :userKey")
    default Page<ProviderAccountSubscriptionDto> findAllObjectsByProvider(@Param("userKey") UUID userKey, Pageable pageable) {
        return this.findAllEntitiesByProvider(userKey, pageable).map(e -> e.toProviderDto());
    }

}
