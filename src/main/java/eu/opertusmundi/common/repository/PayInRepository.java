package eu.opertusmundi.common.repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

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
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInStatusEntity;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
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
    
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE i.order.key = key")
    Optional<PayInEntity> findOneByOrderKey(@Param("key") UUID key);
    
    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.key = :payInKey and p.account.id = :userId")
    Optional<PayInEntity> findOneByAccountIdAndKey(@Param("userId") Integer userId, @Param("payInKey") UUID payInKey);

    @Query("SELECT p FROM PayIn p JOIN FETCH p.items i WHERE p.payIn = :payIn")
    Optional<PayInEntity> findOneByPayInId(@Param("payIn") String payIn);

    @Modifying
    @Query("UPDATE Order o SET o.payin = :payin where o.id = :orderId")
    void setOrderPayIn(@Param("payin") PayInEntity payin, @Param("orderId") Integer orderId);
    
    default PayInDto createBankwirePayInForOrder(BankwirePayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");
        
        final OrderEntity order = this.findOrderByKey(command.getOrderKey()).orElse(null);
        final AccountEntity account = order.getAccount();
        final BankWirePayInEntity payin = new BankWirePayInEntity();
        

        payin.setAccount(account);
        payin.setBankAccount(BankAccountEmbeddable.from(command.getBankAccount()));
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
        
        final PayInItemEntity item = new PayInItemEntity();
        item.setIndex(1);
        item.setOrder(order);
        item.setPayin(payin);
        item.setType(EnumPaymentItemType.ORDER);

        payin.getItems().add(item);

        this.saveAndFlush(payin);
        
        this.setOrderPayIn(payin, order.getId());
                
        return payin.toDto();
    }
    
    default PayInDto createCardDirectPayInForOrder(CardDirectPayInCommand command) throws Exception {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOrderKey(), "Expected a non-null order key");
        
        final OrderEntity order = this.findOrderByKey(command.getOrderKey()).orElse(null);
        final AccountEntity account = order.getAccount();
        final CardDirectPayInEntity payin = new CardDirectPayInEntity();
        

        payin.setAccount(account);
        // Do not save card alias to our database!
        payin.setCard(command.getCardId());
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
        
        final PayInItemEntity item = new PayInItemEntity();
        item.setIndex(1);
        item.setOrder(order);
        item.setPayin(payin);
        item.setType(EnumPaymentItemType.ORDER);

        payin.getItems().add(item);

        this.saveAndFlush(payin);
        
        this.setOrderPayIn(payin, order.getId());
                
        return payin.toDto();
    }

    default PayInDto updatePayInStatus(PayInStatusUpdateCommand command) throws PaymentException {
        final PayInEntity payIn = this.findOneByPayInId(command.getProviderPayInId()).orElse(null);

        if (command.getExecutedOn() != null) {
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            payIn.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(command.getExecutedOn()), ZoneOffset.UTC));

            payIn.setStatus(command.getStatus());
            payIn.setStatusUpdatedOn(payIn.getExecutedOn());
        } else {
            payIn.setStatus(command.getStatus());
            payIn.setStatusUpdatedOn(ZonedDateTime.now());
        }
        payIn.setResultCode(command.getResultCode());
        payIn.setResultMessage(command.getResultMessage());

        final PayInStatusEntity status = new PayInStatusEntity();
        status.setPayin(payIn);
        status.setStatus(payIn.getStatus());
        status.setStatusUpdatedOn(payIn.getStatusUpdatedOn());

        payIn.getStatusHistory().add(status);

        this.saveAndFlush(payIn);

        return payIn.toDto();
    }

}
