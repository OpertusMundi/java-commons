package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;

@Repository
@Transactional(readOnly = true)
public interface AccountSubscriptionRepository extends JpaRepository<AccountSubscriptionEntity, Integer> {

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    List<AccountSubscriptionEntity> findAllByUserKey(@Param("userKey") UUID userKey);

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey")
    default List<AccountSubscriptionDto> findAllByUserKeyForConsumer(@Param("userKey") UUID userKey) {
        return this.findAllByUserKey(userKey).stream()
            .map(e -> e.toConsumerDto())
            .collect(Collectors.toList());
    }

    @Query("SELECT s FROM AccountSubscription s WHERE s.consumer.key = :userKey and s.service = :assetId")
    List<AccountSubscriptionEntity> findAllByUserKeyAndServiceId(@Param("userKey") UUID userKey, @Param("assetId") String assetId);

}
