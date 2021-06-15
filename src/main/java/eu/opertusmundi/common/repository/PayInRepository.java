package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.BankAccountEmbeddable;
import eu.opertusmundi.common.domain.BankWirePayInEntity;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.CartEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInStatusEntity;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface PayInRepository extends JpaRepository<PayInEntity, Integer> {

    @Query("SELECT o FROM Order o WHERE o.key = :key")
    Optional<OrderEntity> findOrderByKey(UUID key);

    @Query("SELECT c FROM Cart c WHERE c.id = : id")
    Optional<CartEntity> findCartById(Integer id);

    @Query("SELECT p FROM PayIn p WHERE p.key = :key")
    Optional<PayInEntity> findOneEntityByKey(@Param("key") UUID key);

    default Optional<PayInDto> findOneObjectByKey(UUID key, boolean includeHelpdeskData) {
        return this.findOneEntityByKey(key).map(o -> o.toDto(true, includeHelpdeskData));
    }

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE i.order.key = key")
    Optional<PayInEntity> findOneByOrderKey(@Param("key") UUID key);

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.key = :payInKey and p.consumer.id = :userId")
    Optional<PayInEntity> findOneByAccountIdAndKey(@Param("userId") Integer userId, @Param("payInKey") UUID payInKey);

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.payIn = :payIn")
    Optional<PayInEntity> findOneByPayInId(@Param("payIn") String payIn);

    @Query("SELECT p FROM PayIn p WHERE (:status IS NULL or p.status = :status) and (p.consumer.key = :userKey)")
    Page<PayInEntity> findAllConsumerPayIns(
        @Param("userKey") UUID userKey, @Param("status") EnumTransactionStatus status, Pageable pageable
    );

    @Query(
        "SELECT p "
      + "FROM   PayIn p "
      + "WHERE (p.status in :status or :status is null) and "
      + "      (p.consumer.email = :email or :email IS NULL) and "
      + "      (:referenceNumber IS NULL or p.referenceNumber = :referenceNumber) "
    )
    Page<PayInEntity> findAllPayInEntities(
        @Param("status") Set<EnumTransactionStatus> status,
        @Param("email") String email,
        @Param("referenceNumber") String referenceNumber,
        Pageable pageable
    );

    default Page<PayInDto> findAllPayInObjects(
        @Param("status") Set<EnumTransactionStatus> status,
        @Param("email") String email,
        @Param("referenceNumber") String referenceNumber,
        Pageable pageable
    ) {
        final Page<PayInEntity> page = this.findAllPayInEntities(
            status != null && status.size() > 0 ? status : null,
            StringUtils.isBlank(email) ? null : email,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            pageable
        );

        return page.map(e -> e.toDto(true, true));
    }


    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE PayIn p SET p.processDefinition = :processDefinition, p.processInstance = :processInstance WHERE p.id = :id")
    void setPayInWorkflowInstance(
        @Param("id")                Integer id,
        @Param("processDefinition") String processDefinition,
        @Param("processInstance")   String processInstance
    );

    @Transactional(readOnly = false)
    default PayInDto createBankwirePayInForOrder(BankwirePayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

        final OrderEntity         order    = this.findOrderByKey(command.getOrderKey()).orElse(null);
        final AccountEntity       consumer = order.getConsumer();
        final BankWirePayInEntity payin    = new BankWirePayInEntity();


        payin.setBankAccount(BankAccountEmbeddable.from(command.getBankAccount()));
        payin.setConsumer(consumer);
        payin.setCreatedOn(command.getCreatedOn());
        payin.setCurrency(order.getCurrency());
        payin.setKey(command.getPayInKey());
        payin.setPayIn(command.getPayIn());
        payin.setReferenceNumber(command.getReferenceNumber());
        payin.setStatus(EnumTransactionStatus.CREATED);
        payin.setStatusUpdatedOn(payin.getCreatedOn());
        payin.setTotalPrice(order.getTotalPrice());
        payin.setTotalPriceExcludingTax(order.getTotalPriceExcludingTax());
        payin.setTotalTax(order.getTotalTax());
        payin.setWireReference(command.getWireReference());

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payin);
        status.setStatus(payin.getStatus());
        status.setStatusUpdatedOn(payin.getStatusUpdatedOn());

        payin.getStatusHistory().add(status);

        final PayInOrderItemEntity item = new PayInOrderItemEntity();
        item.setIndex(1);
        item.setOrder(order);
        item.setPayin(payin);

        payin.getItems().add(item);

        this.saveAndFlush(payin);

        return payin.toDto();
    }

    @Transactional(readOnly = false)
    default PayInDto createCardDirectPayInForOrder(CardDirectPayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

        final OrderEntity           order    = this.findOrderByKey(command.getOrderKey()).orElse(null);
        final AccountEntity         consumer = order.getConsumer();
        final CardDirectPayInEntity payin    = new CardDirectPayInEntity();

        // Do not save card alias to our database!
        payin.setCard(command.getCardId());
        payin.setConsumer(consumer);
        payin.setCreatedOn(command.getCreatedOn());
        payin.setCurrency(order.getCurrency());
        payin.setExecutedOn(command.getExecutedOn());
        payin.setKey(command.getPayInKey());
        payin.setPayIn(command.getPayIn());
        payin.setReferenceNumber(command.getReferenceNumber());
        payin.setResultCode(command.getResultCode());
        payin.setResultMessage(command.getResultMessage());
        payin.setStatementDescriptor(command.getStatementDescriptor());
        payin.setStatus(command.getStatus());
        payin.setStatusUpdatedOn(payin.getExecutedOn() == null ? payin.getCreatedOn() : payin.getExecutedOn());
        payin.setTotalPrice(order.getTotalPrice());
        payin.setTotalPriceExcludingTax(order.getTotalPriceExcludingTax());
        payin.setTotalTax(order.getTotalTax());

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payin);
        status.setStatus(payin.getStatus());
        status.setStatusUpdatedOn(payin.getStatusUpdatedOn());

        payin.getStatusHistory().add(status);

        final PayInOrderItemEntity item = new PayInOrderItemEntity();
        item.setIndex(1);
        item.setOrder(order);
        item.setPayin(payin);

        payin.getItems().add(item);

        this.saveAndFlush(payin);

        return payin.toDto();
    }

    @Transactional(readOnly = false)
    default PayInDto updatePayInStatus(PayInStatusUpdateCommand command) throws PaymentException {
        final PayInEntity payIn = this.findOneByPayInId(command.getProviderPayInId()).orElse(null);

        // Update PayIn
        if (command.getExecutedOn() != null) {
            payIn.setExecutedOn(command.getExecutedOn());
        }

        payIn.setResultCode(command.getResultCode());
        payIn.setResultMessage(command.getResultMessage());
        payIn.setStatus(command.getStatus());
        payIn.setStatusUpdatedOn(ZonedDateTime.now());

        // Create status history record
        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payIn);
        status.setStatus(payIn.getStatus());
        status.setStatusUpdatedOn(payIn.getStatusUpdatedOn());

        payIn.getStatusHistory().add(status);

        this.saveAndFlush(payIn);

        return payIn.toDto();
    }

}
