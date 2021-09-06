package eu.opertusmundi.common.repository;

import java.math.BigDecimal;
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
import eu.opertusmundi.common.domain.BankAccountEmbeddable;
import eu.opertusmundi.common.domain.BankWirePayInEntity;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.CartEntity;
import eu.opertusmundi.common.domain.FreePayInEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInStatusEntity;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.FreePayInCommand;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface PayInRepository extends JpaRepository<PayInEntity, Integer> {

    @Query("SELECT c FROM Cart c WHERE c.id = : id")
    Optional<CartEntity> findCartById(Integer id);

    @Query("SELECT o FROM Order o WHERE o.key = :key")
    Optional<OrderEntity> findOrderByKey(UUID key);

    @Query("SELECT p FROM PayIn p WHERE p.key = :key")
    Optional<PayInEntity> findOneEntityByKey(@Param("key") UUID key);

    default Optional<PayInDto> findOneObjectByKey(UUID key) {
        return this.findOneEntityByKey(key).map(o -> o.toHelpdeskDto(true));
    }

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE i.order.key = key")
    Optional<PayInEntity> findOneByOrderKey(@Param("key") UUID key);

    /**
     * Find a consumer PayIn
     *
     * This method does not return PayIn records with payment method
     * <b>CARD_DIRECT</b> which have status <b>CREATED</b>.
     *
     * @param userId
     * @param payInKey
     * @return
     */
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i "
         + "WHERE  p.key = :payInKey and "
         + "       p.consumer.id = :userId and "
         + "       (p.status <> 'CREATED' or p.paymentMethod <> 'CARD_DIRECT')"
    )
    Optional<PayInEntity> findOneByConsumerIdAndKey(@Param("userId") Integer userId, @Param("payInKey") UUID payInKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.payIn = :payIn")
    Optional<PayInEntity> findOneByPayInId(@Param("payIn") String payIn);

    @Query("SELECT i FROM PayInItem i WHERE i.payin.key = :payInKey and i.provider.id = :userId and i.index = :index")
    Optional<PayInItemEntity> findOnePayInItemByProvider(
        @Param("userId") Integer userId, @Param("payInKey") UUID payInKey, @Param("index") Integer index
    );

    @Query("SELECT i FROM PayInItem i WHERE i.transfer = :transferId")
    Optional<PayInItemEntity> findOnePayInItemByTransferId(@Param("transferId") String transferId);

    /**
     * Query consumer PayIns
     *
     * This method does not return PayIn records with payment method
     * <b>CARD_DIRECT</b> which have status <b>CREATED</b>.
     *
     * @param userKey
     * @param status
     * @param pageable
     * @return
     */
    @Query("SELECT p FROM PayIn p "
         + "WHERE (:status IS NULL or p.status = :status) and "
         + "      (p.consumer.key = :userKey) and "
         + "      (p.status <> 'CREATED' or p.paymentMethod <> 'CARD_DIRECT')"
    )
    Page<PayInEntity> findAllConsumerPayIns(
        @Param("userKey") UUID userKey, @Param("status") EnumTransactionStatus status, Pageable pageable
    );

    @Query("SELECT i FROM PayInItem i WHERE (:status IS NULL or i.payin.status = :status) and (i.provider.key = :userKey)")
    Page<PayInItemEntity> findAllProviderPayInItems(
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

    default Page<HelpdeskPayInDto> findAllPayInObjects(
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

        return page.map(e -> e.toHelpdeskDto(true));
    }

    @Query(
        "SELECT i "
      + "FROM   PayInItem i "
      + "WHERE (i.transferStatus in :status or :status is null) and "
      + "      (i.transfer is not null) and "
      + "      (:referenceNumber IS NULL or i.payin.referenceNumber = :referenceNumber) "
    )
    Page<PayInItemEntity> findAllTransferEntities(
        @Param("status") Set<EnumTransactionStatus> status,
        @Param("referenceNumber") String referenceNumber,
        Pageable pageable
    );

    default Page<PayInItemDto> findAllTransferObjects(
        @Param("status") Set<EnumTransactionStatus> status,
        @Param("referenceNumber") String referenceNumber,
        Pageable pageable
    ) {
        final Page<PayInItemEntity> page = this.findAllTransferEntities(
            status != null && status.size() > 0 ? status : null,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            pageable
        );

        return page.map(i -> i.toHelpdeskDto(true));
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
    default PayInDto createFreePayInForOrder(FreePayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

        final OrderEntity order = this.findOrderByKey(command.getOrderKey()).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(order.getItems().size() == 1, "Expected a single order item");
        Assert.isTrue(order.getTotalPrice().compareTo(BigDecimal.ZERO) == 0, "Expected total price to be 0");

        final AccountEntity   consumer = order.getConsumer();
        final FreePayInEntity payin    = new FreePayInEntity();
        final ZonedDateTime   now      = ZonedDateTime.now();

        payin.setConsumer(consumer);
        payin.setCreatedOn(now);
        payin.setCurrency(order.getCurrency());
        payin.setExecutedOn(now);
        payin.setKey(command.getPayInKey());
        payin.setPayIn(UUID.randomUUID().toString());
        payin.setReferenceNumber(command.getReferenceNumber());
        payin.setStatus(EnumTransactionStatus.SUCCEEDED);
        payin.setStatusUpdatedOn(now);
        payin.setTotalPrice(order.getTotalPrice());
        payin.setTotalPriceExcludingTax(order.getTotalPriceExcludingTax());
        payin.setTotalTax(order.getTotalTax());

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payin);
        status.setStatus(payin.getStatus());
        status.setStatusUpdatedOn(now);

        payin.getStatusHistory().add(status);

        final PayInOrderItemEntity item = new PayInOrderItemEntity();
        item.setIndex(1);
        item.setOrder(order);
        item.setPayin(payin);
        item.setProvider(order.getItems().get(0).getProvider());

        payin.getItems().add(item);

        this.saveAndFlush(payin);

        return payin.toConsumerDto(true);
    }

    @Transactional(readOnly = false)
    default PayInDto createBankwirePayInForOrder(BankwirePayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

        final OrderEntity order = this.findOrderByKey(command.getOrderKey()).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(order.getItems().size() == 1, "Expected a single order item");

        final AccountEntity       consumer = order.getConsumer();
        final BankWirePayInEntity payin    = new BankWirePayInEntity();

        payin.setBankAccount(BankAccountEmbeddable.from(command.getBankAccount()));
        payin.setConsumer(consumer);
        payin.setCreatedOn(command.getCreatedOn());
        payin.setCurrency(order.getCurrency());
        payin.setExecutedOn(command.getExecutedOn());
        payin.setKey(command.getPayInKey());
        payin.setPayIn(command.getPayIn());
        payin.setReferenceNumber(command.getReferenceNumber());
        payin.setResultCode(command.getResultCode());
        payin.setResultMessage(command.getResultMessage());
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
        item.setProvider(order.getItems().get(0).getProvider());

        payin.getItems().add(item);

        this.saveAndFlush(payin);

        return payin.toConsumerDto(true);
    }

    @Transactional(readOnly = false)
    default ConsumerPayInDto createCardDirectPayInForOrder(CardDirectPayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");

        final OrderEntity order = this.findOrderByKey(command.getOrderKey()).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(order.getItems().size() == 1, "Expected a single order item");

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
        item.setProvider(order.getItems().get(0).getProvider());

        payin.getItems().add(item);

        this.saveAndFlush(payin);

        return payin.toConsumerDto(true);
    }

    /**
     * Update PayIn status
     *
     * PayIn is updated only when status changes.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    @Transactional(readOnly = false)
    default HelpdeskPayInDto updatePayInStatus(PayInStatusUpdateCommand command) throws PaymentException {
        final PayInEntity payIn = this.findOneByPayInId(command.getProviderPayInId()).orElse(null);

        // Update only on status changes
        if (payIn.getStatus() == command.getStatus()) {
            return payIn.toHelpdeskDto(true);
        }

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

        return payIn.toHelpdeskDto(true);
    }

}
