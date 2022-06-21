package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.LockModeType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.domain.PayOutStatusEntity;
import eu.opertusmundi.common.model.EnumReferenceType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PayOutStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.util.MangopayUtils;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface PayOutRepository extends JpaRepository<PayOutEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);

    @Query("SELECT p FROM PayOut p WHERE p.key = :key")
    Optional<PayOutEntity> findOneEntityByKey(@Param("key") UUID key);

    default Optional<PayOutDto> findOneObjectByKey(UUID key, boolean includeHelpdeskData) {
        return this.findOneEntityByKey(key).map(o -> o.toDto(includeHelpdeskData));
    }

    @Query("SELECT p FROM PayOut p WHERE p.key = :payOutKey and p.provider.id = :providerId")
    Optional<PayOutEntity> findOneByAccountIdAndKey(@Param("providerId") Integer userId, @Param("payOutKey") UUID payOutKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayOut p WHERE p.payOut = :payOut")
    Optional<PayOutEntity> findOneByPayOutId(@Param("payOut") String payOut);


    @Query("SELECT count(p) FROM PayOut p WHERE (p.status not in ('FAILED', 'SUCCEEDED')) and (p.provider.key = :userKey)")
    long countProviderPendingPayOuts(@Param("userKey") UUID userKey);

    @Query("SELECT p FROM PayOut p WHERE (:status IS NULL or p.status = :status) and (p.provider.key = :userKey)")
    Page<PayOutEntity> findAllProviderPayOuts(
        @Param("userKey") UUID userKey, @Param("status") EnumTransactionStatus status, Pageable pageable
    );

    @Query(
        "SELECT p "
      + "FROM   PayOut p "
      + "WHERE (p.status in :status or :status is null) and "
      + "      (p.provider.key = :providerKey or cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL) and "
      + "      (p.provider.email like :providerEmail or :providerEmail IS NULL) and "
      + "      (:bankwireRef IS NULL or p.bankwireRef = :bankwireRef) "
    )
    Page<PayOutEntity> findAllPayOutEntities(
        UUID providerKey,
        String providerEmail,
        Set<EnumTransactionStatus> status,
        String bankwireRef,
        Pageable pageable
    );

    default Page<PayOutDto> findAllPayOutObjects(
        UUID providerKey,
        String providerEmail,
        Set<EnumTransactionStatus> status,
        @Param("bankwireRef") String bankwireRef,
        Pageable pageable
    ) {
        final Page<PayOutEntity> page = this.findAllPayOutEntities(
            providerKey,
            StringUtils.isBlank(providerEmail) ? null : providerEmail,
            status != null && status.size() > 0 ? status : null,
            StringUtils.isBlank(bankwireRef) ? null : bankwireRef,
            pageable
        );

        return page.map(e -> e.toDto(true));
    }

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE PayOut p SET p.processDefinition = :processDefinition, p.processInstance = :processInstance WHERE p.id = :id")
    void setPayOutWorkflowInstance(
        @Param("id")                Integer id,
        @Param("processDefinition") String processDefinition,
        @Param("processInstance")   String processInstance
    );


    @Transactional(readOnly = false)
    default PayOutDto createPayOut(PayOutCommandDto command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getProviderKey(), "Expected a non-null provider key");

        final AccountEntity provider = this.findAccountByKey(command.getProviderKey()).orElse(null);

        Assert.notNull(provider, "Expected a non-null provider");

        final PayOutEntity payout = new PayOutEntity();

        payout.setBankAccount(command.getBankAccount());
        payout.setBankwireRef("N/A");
        payout.setCurrency("EUR");
        payout.setDebitedFunds(command.getDebitedFunds());
        payout.setPlatformFees(command.getFees());
        payout.setProvider(provider);
        payout.setStatus(EnumTransactionStatus.NotSpecified);
        payout.setStatusUpdatedOn(ZonedDateTime.now());

        final PayOutStatusEntity status = new PayOutStatusEntity();
        status.setPayout(payout);
        status.setStatus(payout.getStatus());
        status.setStatusUpdatedOn(payout.getStatusUpdatedOn());

        payout.getStatusHistory().add(status);

        this.saveAndFlush(payout);

        payout.setBankwireRef(MangopayUtils.createReferenceNumber(EnumReferenceType.PAYOUT, payout.getId()));

        return payout.toDto();
    }

    /**
     * Update PayOut status
     *
     * PayOut is updated only when status changes.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    @Transactional(readOnly = false)
    default PayOutDto updatePayOutStatus(PayOutStatusUpdateCommand command) throws PaymentException {
        final PayOutEntity payOut = this.findOneEntityByKey(command.getKey()).orElse(null);

        // Update only on status changes
        if (payOut.getStatus() == command.getStatus()) {
            return payOut.toDto(true);
        }

        // Update PayOut
        if (StringUtils.isBlank(payOut.getPayOut())) {
            payOut.setPayOut(command.getProviderPayOutId());
        } else {
            Assert.isTrue(payOut.getPayOut().equals(command.getProviderPayOutId()), "PayOut identifier is not updatable");
        }
        if (command.getCreatedOn() != null) {
            payOut.setCreatedOn(command.getCreatedOn());
        }
        if (command.getExecutedOn() != null) {
            payOut.setExecutedOn(command.getExecutedOn());
        }

        payOut.setResultCode(command.getResultCode());
        payOut.setResultMessage(command.getResultMessage());
        payOut.setStatus(command.getStatus());
        payOut.setStatusUpdatedOn(ZonedDateTime.now());

        // Create status history record
        final PayOutStatusEntity status = new PayOutStatusEntity();
        status.setPayout(payOut);
        status.setStatus(payOut.getStatus());
        status.setStatusUpdatedOn(payOut.getStatusUpdatedOn());

        payOut.getStatusHistory().add(status);

        this.saveAndFlush(payOut);

        return payOut.toDto(true);
    }

}
