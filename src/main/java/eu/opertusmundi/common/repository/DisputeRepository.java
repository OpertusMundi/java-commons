package eu.opertusmundi.common.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.DisputeEntity;
import eu.opertusmundi.common.model.payment.DisputeDto;
import eu.opertusmundi.common.model.payment.EnumDisputeStatus;

@Repository
@Transactional(readOnly = true)
public interface DisputeRepository extends JpaRepository<DisputeEntity, Integer> {

    @Query("SELECT d FROM Dispute d WHERE d.transactionId = :transactionId")
    Optional<DisputeEntity> findOneByTransactionId(String transactionId);

    @Query("""
        SELECT d 
        FROM   Dispute d 
        WHERE (d.status in :status or :status is null) 
    """)
    Page<DisputeEntity> findAllEntities(Set<EnumDisputeStatus> status, Pageable pageable);

    default Page<DisputeDto> findAllObjects(Set<EnumDisputeStatus> status, Pageable pageable) {
        final var result = this
            .findAllEntities(status, pageable)
            .map(e -> e.toDto(true));

        return result;
    }
}
