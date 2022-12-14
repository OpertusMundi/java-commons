package eu.opertusmundi.common.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountRoleEntity;
import eu.opertusmundi.common.domain.BankAccountEmbeddable;
import eu.opertusmundi.common.domain.BankWirePayInEntity;
import eu.opertusmundi.common.domain.BillingAddressEmbeddable;
import eu.opertusmundi.common.domain.BrowserInfoEmbeddable;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.CartEntity;
import eu.opertusmundi.common.domain.FreePayInEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationEntity;
import eu.opertusmundi.common.domain.PayInStatusEntity;
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.domain.ShippingAddressEmbeddable;
import eu.opertusmundi.common.domain.ServiceBillingEntity;
import eu.opertusmundi.common.model.EnumReferenceType;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.BankwirePayInExecutionContext;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInExecutionContext;
import eu.opertusmundi.common.model.payment.CheckoutServiceBillingCommandDto;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.FreePayInCommand;
import eu.opertusmundi.common.model.payment.FreePayInExecutionContext;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInItemDto;
import eu.opertusmundi.common.util.MangopayUtils;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface PayInRepository extends JpaRepository<PayInEntity, Integer> {

    @Query("SELECT b FROM ServiceBilling b WHERE (b.key in :keys)")
    List<ServiceBillingEntity> findServiceBillingRecords(List<UUID> keys);

    @Query("SELECT c FROM Cart c WHERE c.id = : id")
    Optional<CartEntity> findCartById(Integer id);

    @Query("SELECT o FROM Order o WHERE o.key = :key")
    Optional<OrderEntity> findOrderByKey(UUID key);

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT p FROM PayIn p WHERE p.key = :key")
    Optional<PayInEntity> findOneEntityByKey(UUID key);

    default Optional<HelpdeskPayInDto> findOneObjectByKey(UUID key) {
        return this.findOneEntityByKey(key).map(p -> p.toHelpdeskDto(true));
    }

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE i.order.key = :key")
    Optional<PayInEntity> findOneByOrderKey(UUID key);

    @Query("SELECT r FROM PayInRecurringRegistration r WHERE r.providerRegistration = :id")
    Optional<PayInRecurringRegistrationEntity> findRecurringRegistrationById(String id);

    /**
     * Find a consumer PayIn
     *
     * This method does not return PayIn records with (a) status
     * <b>NotSpecified</> and (b) payment method <b>CARD_DIRECT</b> with status
     * <b>CREATED</b>.
     *
     * @param userId
     * @param payInKey
     * @return
     */
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i "
         + "WHERE  (p.key = :payInKey) and "
         + "       (p.consumer.id = :userId) and "
         + "       (p.status <> 'NotSpecified') and "
         + "       (p.status <> 'CREATED' or p.paymentMethod <> 'CARD_DIRECT')"
    )
    Optional<PayInEntity> findOneByConsumerIdAndKey(Integer userId, UUID payInKey);

    /**
     * Find a prepared PayIn
     *
     * <p>
     * This method returns only prepared PayIn records with payment method
     * <b>CARD_DIRECT</b> and status <b>NotSpecified</b>.
     *
     * @param userId
     * @param payInKey
     * @return
     */
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i "
         + "WHERE  p.key = :payInKey and "
         + "       p.consumer.key = :userKey and "
         + "       p.status = 'NotSpecified' and "
         + "       p.paymentMethod = 'CARD_DIRECT'"
    )
    Optional<CardDirectPayInEntity> findOnePrepared(UUID userKey, UUID payInKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.payIn = :payIn")
    Optional<PayInEntity> findOneByPayInId(String payIn);

    @Query("SELECT i FROM PayInItem i WHERE i.payin.key = :payInKey and i.provider.id = :userId and i.index = :index")
    Optional<PayInItemEntity> findOnePayInItemByProvider(Integer userId, UUID payInKey, Integer index);

    @Query("SELECT i FROM PayInItem i WHERE i.transfer = :transferId")
    Optional<PayInItemEntity> findOnePayInItemByTransferId(String transferId);

    /**
     * Query consumer PayIns
     *
     * <br />
     * <br />
     *
     * This method does not return PayIn records with (a) status
     * <b>NotSpecified</> and (b) payment method <b>CARD_DIRECT</b> with status
     * <b>CREATED</b>.
     *
     * @param userKey
     * @param status
     * @param pageable
     * @return
     */
    @Query("SELECT p FROM PayIn p "
         + "WHERE (:status IS NULL or p.status = :status) and "
         + "      (p.consumer.key = :consumerKey) and "
         + "      (p.status <> 'NotSpecified') and "
         + "      (p.status <> 'CREATED' or p.paymentMethod <> 'CARD_DIRECT') and "
         + "      (:referenceNumber IS NULL or p.referenceNumber = :referenceNumber) "
    )
    Page<PayInEntity> findAllConsumerPayIns(
        UUID consumerKey, String referenceNumber, EnumTransactionStatus status, Pageable pageable
    );

    default Page<ConsumerPayInDto> findAllObjectsConsumerPayIns(
        UUID consumerKey, String referenceNumber, EnumTransactionStatus status,
        Pageable pageable,
        boolean includeDetails
    ) {
        return this.findAllConsumerPayIns(
            consumerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status,
            pageable
        ).map(e -> e.toConsumerDto(includeDetails));
    }

    @Query("SELECT  i "
         + "FROM    PayInItem i "
         + "WHERE   (:status IS NULL or i.payin.status in :status) and "
         + "        (i.provider.key = :userKey) and "
         + "        (:referenceNumber IS NULL or i.payin.referenceNumber = :referenceNumber)")
    Page<PayInItemEntity> findAllProviderPayInItems(
        UUID userKey, String referenceNumber, Set<EnumTransactionStatus> status, Pageable pageable
    );

    default Page<HelpdeskPayInItemDto> findAllObjectsProviderPayInItems(
        UUID userKey, String referenceNumber, Set<EnumTransactionStatus> status, Pageable pageable
    ) {
        final Page<PayInItemEntity> page = this.findAllProviderPayInItems(
            userKey, referenceNumber, status, pageable
        );

        return page.map(e -> e.toHelpdeskDto(true));
    }


    @Query(
        "SELECT p "
      + "FROM   PayIn p "
      + "WHERE (p.status in :status or :status is NULL) and "
      + "      (cast(:consumerKey as org.hibernate.type.UUIDCharType) IS NULL or p.consumer.key = :consumerKey) and "
      + "      (:consumerEmail IS NULL or p.consumer.email like :consumerEmail) and "
      + "      (:referenceNumber IS NULL or p.referenceNumber = :referenceNumber)"
    )
    Page<PayInEntity> findAllPayInEntities(
        UUID consumerKey,
        String consumerEmail,
        String referenceNumber,
        Set<EnumTransactionStatus> status,
        Pageable pageable
    );

    default Page<HelpdeskPayInDto> findAllPayInObjects(
        UUID consumerKey,
        String consumerEmail,
        String referenceNumber,
        Set<EnumTransactionStatus> status,
        Pageable pageable
    ) {
        final Page<PayInEntity> page = this.findAllPayInEntities(
            consumerKey,
            StringUtils.isBlank(consumerEmail) ? null : consumerEmail,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        );

        return page.map(e -> e.toHelpdeskDto(true));
    }

    @Query(
        "SELECT i "
      + "FROM   PayInItem i "
      + "WHERE (i.transferStatus in :status or :status is null) and "
      + "      (cast(:providerKey as org.hibernate.type.UUIDCharType) IS NULL or i.provider.key = :providerKey) and "
      + "      (i.transfer is not null) and "
      + "      (:referenceNumber IS NULL or i.payin.referenceNumber = :referenceNumber) "
    )
    Page<PayInItemEntity> findAllTransferEntities(
        UUID providerKey,
        Set<EnumTransactionStatus> status,
        String referenceNumber,
        Pageable pageable
    );

    default Page<PayInItemDto> findAllTransferObjects(
        UUID providerKey,
        Set<EnumTransactionStatus> status,
        String referenceNumber,
        Pageable pageable
    ) {
        final Page<PayInItemEntity> page = this.findAllTransferEntities(
            providerKey,
            status != null && status.size() > 0 ? status : null,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            pageable
        );

        return page.map(i -> i.toHelpdeskDto(true));
    }

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE PayIn p SET p.processDefinition = :processDefinition, p.processInstance = :processInstance WHERE p.id = :id")
    void setPayInWorkflowInstance(Integer id, String processDefinition, String processInstance);

    @Transactional(readOnly = false)
    default PayInDto createFreePayInForOrder(FreePayInExecutionContext ctx) throws Exception {
        Assert.notNull(ctx, "Expected a non-null context");

        final FreePayInCommand command = ctx.getCommand();
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getKey(), "Expected a non-null order key");

        final OrderEntity order = this.findOrderByKey(command.getKey()).orElse(null);

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
        payin.setKey(command.getKey());
        payin.setPayIn(UUID.randomUUID().toString());
        payin.setReferenceNumber(ctx.getReferenceNumber());
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
    default PayInDto createBankwirePayInForOrder(BankwirePayInExecutionContext ctx) throws Exception {
        Assert.notNull(ctx, "Expected a non-null context");

        final BankwirePayInCommand command = ctx.getCommand();
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getKey(), "Expected a non-null payin key");

        final OrderEntity order = this.findOrderByKey(command.getKey()).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(order.getItems().size() == 1, "Expected a single order item");

        final AccountEntity       consumer = order.getConsumer();
        final BankWirePayInEntity payin    = new BankWirePayInEntity();

        payin.setBankAccount(BankAccountEmbeddable.from(ctx.getBankAccount()));
        payin.setConsumer(consumer);
        payin.setCreatedOn(ctx.getCreatedOn());
        payin.setCurrency(order.getCurrency());
        payin.setExecutedOn(ctx.getExecutedOn());
        payin.setKey(command.getKey());
        payin.setPayIn(ctx.getPayIn());
        payin.setReferenceNumber(ctx.getReferenceNumber());
        payin.setResultCode(ctx.getResultCode());
        payin.setResultMessage(ctx.getResultMessage());
        payin.setStatus(EnumTransactionStatus.CREATED);
        payin.setStatusUpdatedOn(payin.getCreatedOn());
        payin.setTotalPrice(order.getTotalPrice());
        payin.setTotalPriceExcludingTax(order.getTotalPriceExcludingTax());
        payin.setTotalTax(order.getTotalTax());
        payin.setWireReference(ctx.getWireReference());

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
    default ConsumerPayInDto createCardDirectPayInForOrder(CardDirectPayInExecutionContext ctx) throws Exception {
        Assert.notNull(ctx, "Expected a non-null context");

        final CardDirectPayInCommand command = ctx.getCommand();
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getKey(), "Expected a non-null order key");

        final OrderEntity order = this.findOrderByKey(command.getKey()).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(order.getItems().size() == 1, "Expected a single order item");

        final PayInRecurringRegistrationEntity registration = this.findRecurringRegistrationById(ctx.getRecurringPayinRegistrationId())
            .orElse(null);

        final AccountEntity         consumer = order.getConsumer();
        final CardDirectPayInEntity payin    = new CardDirectPayInEntity();

        // Do not save card alias to our database!
        payin.setApplied3dsVersion(ctx.getApplied3dsVersion());
        payin.setCard(command.getCardId());
        payin.setConsumer(consumer);
        payin.setCreatedOn(ctx.getCreatedOn());
        payin.setCurrency(order.getCurrency());
        payin.setExecutedOn(ctx.getExecutedOn());
        payin.setIpAddress(command.getIpAddress());
        payin.setKey(command.getKey());
        payin.setPayIn(ctx.getPayIn());
        payin.setReferenceNumber(ctx.getReferenceNumber());
        payin.setRequested3dsVersion(ctx.getRequested3dsVersion());
        payin.setResultCode(ctx.getResultCode());
        payin.setResultMessage(ctx.getResultMessage());
        payin.setStatementDescriptor(ctx.getStatementDescriptor());
        payin.setStatus(ctx.getStatus());
        payin.setStatusUpdatedOn(payin.getExecutedOn() == null ? payin.getCreatedOn() : payin.getExecutedOn());
        payin.setTotalPrice(order.getTotalPrice());
        payin.setTotalPriceExcludingTax(order.getTotalPriceExcludingTax());
        payin.setTotalTax(order.getTotalTax());
        payin.setRecurringPaymentType(ctx.getRecurringTransactionType());
        payin.setRecurringPayment(registration);

        if (command.getBilling() != null) {
            payin.setBillingAddress(BillingAddressEmbeddable.from(command.getBilling()));
        }
        if (command.getShipping() != null) {
            payin.setShippingAddress(ShippingAddressEmbeddable.from(command.getShipping()));
        }
        if (command.getBrowserInfo() != null) {
            payin.setBrowserInfo(BrowserInfoEmbeddable.from(command.getBrowserInfo()));
        }

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
     * Creates a new PayIn with status
     * {@link EnumTransactionStatus#NotSpecified} from a collection of
     * subscription billing record keys
     *
     * @param command
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = false)
    default ConsumerPayInDto prepareCardDirectPayInForServiceBilling(
        CheckoutServiceBillingCommandDto command
    ) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notEmpty(command.getKeys(), "Expected a non-empty subscription billing list");

        final AccountEntity consumer = this.findAccountByKey(command.getUserKey()).orElse(null);
        Assert.notNull(consumer, "Expected a non-null user account");

        final AccountRoleEntity role = consumer.getRoles().stream()
            .filter(r -> r.getRole() == EnumRole.ROLE_CONSUMER)
            .findFirst()
            .orElse(null);
        Assert.notNull(role, "Expected user to be a registered consumer");

        // Do not save card alias to our database!
        final CardDirectPayInEntity payin = new CardDirectPayInEntity();
        payin.setConsumer(consumer);
        payin.setCreatedOn(ZonedDateTime.now());
        payin.setCurrency("EUR");
        payin.setKey(UUID.randomUUID());
        payin.setStatus(EnumTransactionStatus.NotSpecified);
        payin.setStatusUpdatedOn(payin.getCreatedOn());
        payin.setTotalPrice(command.getTotalPrice());
        payin.setTotalPriceExcludingTax(command.getTotalPriceExcludingTax());
        payin.setTotalTax(command.getTotalTax());
        payin.setRecurringPaymentType(EnumRecurringPaymentType.NONE);

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payin);
        status.setStatus(payin.getStatus());
        status.setStatusUpdatedOn(payin.getStatusUpdatedOn());

        payin.getStatusHistory().add(status);

        final var records = this.findServiceBillingRecords(command.getKeys());
        for (int index = 0; index < records.size(); index++) {
            final var record = records.get(index);
            final var item   = new PayInServiceBillingItemEntity();
            item.setIndex(index + 1);
            item.setPayin(payin);
            final var owner = switch (record.getType()) {
                case SUBSCRIPTION -> record.getSubscription().getProvider();
                case PRIVATE_OGC_SERVICE -> record.getUserService().getAccount().getParent() == null
                    ? record.getUserService().getAccount()
                    : record.getUserService().getAccount().getParent();
            };
            item.setProvider(owner);
            item.setServiceBilling(record);

            //record.setStatus(EnumServiceBillingStatus.PROCESSING);

            payin.getItems().add(item);
        }

        this.saveAndFlush(payin);

        // Compute reference number from database key
        payin.setReferenceNumber(MangopayUtils.createReferenceNumber(EnumReferenceType.PAYIN, payin.getId()));
        payin.setStatementDescriptor(payin.getReferenceNumber());

        this.saveAndFlush(payin);

        return payin.toConsumerDto(true);
    }

    default ConsumerPayInDto updateCardDirectPayInForServiceBilling(CardDirectPayInExecutionContext ctx) {
        Assert.notNull(ctx, "Expected a non-null context");

        final CardDirectPayInCommand command = ctx.getCommand();
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getKey(), "Expected a non-null PayIn key");

        final CardDirectPayInEntity payin = this.findOnePrepared(command.getUserKey(), command.getKey()).orElse(null);
        Assert.notNull(payin, "Expected a non-null payin");

        // Do not save card alias to our database!
        payin.setApplied3dsVersion(ctx.getApplied3dsVersion());
        payin.setCard(command.getCardId());
        payin.setExecutedOn(ctx.getExecutedOn());
        payin.setIpAddress(command.getIpAddress());
        payin.setPayIn(ctx.getPayIn());
        payin.setRequested3dsVersion(ctx.getRequested3dsVersion());
        payin.setResultCode(ctx.getResultCode());
        payin.setResultMessage(ctx.getResultMessage());
        payin.setStatus(ctx.getStatus());
        payin.setStatusUpdatedOn(payin.getExecutedOn() == null ? ZonedDateTime.now() : payin.getExecutedOn());

        if (command.getBilling() != null) {
            payin.setBillingAddress(BillingAddressEmbeddable.from(command.getBilling()));
        }
        if (command.getShipping() != null) {
            payin.setShippingAddress(ShippingAddressEmbeddable.from(command.getShipping()));
        }
        if (command.getBrowserInfo() != null) {
            payin.setBrowserInfo(BrowserInfoEmbeddable.from(command.getBrowserInfo()));
        }

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payin);
        status.setStatus(payin.getStatus());
        status.setStatusUpdatedOn(payin.getStatusUpdatedOn());
        payin.getStatusHistory().add(status);

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
