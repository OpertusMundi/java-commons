package eu.opertusmundi.common.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.RefundEntity;
import eu.opertusmundi.common.model.payment.EnumRefundReasonType;
import eu.opertusmundi.common.model.payment.RefundDto;

@Repository
@Transactional(readOnly = true)
public interface RefundRepository extends JpaRepository<RefundEntity, Integer> {

    @Query("SELECT r FROM Refund r WHERE r.transactionId = :transactionId")
    Optional<RefundEntity> findOneByTransactionId(String transactionId);

    @Query("""
        SELECT r
        FROM   Refund r 
        WHERE (r.refundReasonType in :reason or :reason is null) 
    """)
    Page<RefundEntity> findAllEntities(Set<EnumRefundReasonType> reason, Pageable pageable);

    default Page<RefundDto> findAllObjects(Set<EnumRefundReasonType> reason, Pageable pageable) {
        final var result = this
            .findAllEntities(reason, pageable)
            .map(e -> e.toDto(true));

        return result;
    }
        
}
