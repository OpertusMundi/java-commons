package eu.opertusmundi.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.SubscriptionBillingEntity;

@Repository
@Transactional(readOnly = true)
public interface SubscriptionBillingRepository extends JpaRepository<SubscriptionBillingEntity, Integer> {

}
