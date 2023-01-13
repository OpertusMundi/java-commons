package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.TransferEntity;

@Repository
@Transactional(readOnly = true)
public interface TransferRepository extends JpaRepository<TransferEntity, Integer> {

    @Query("SELECT r FROM Transfer r WHERE r.transactionId = :transactionId")
    Optional<TransferEntity> findOneByTransactionId(String transactionId);

}
