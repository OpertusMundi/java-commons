package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.RefundEntity;

@Repository
@Transactional(readOnly = true)
public interface RefundRepository extends JpaRepository<RefundEntity, Integer> {

    @Query("SELECT r FROM Refund r WHERE r.transactionId = :transactionId")
    Optional<RefundEntity> findOneByTransactionId(String transactionId);

}
