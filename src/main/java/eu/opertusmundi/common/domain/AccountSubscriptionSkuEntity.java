package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import eu.opertusmundi.common.model.account.AccountSubscriptionSkuDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountSubscriptionSku")
@Table(schema = "web", name = "`account_subscription_sku`")
public class AccountSubscriptionSkuEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_subscription_sku_id_seq", name = "account_subscription_sku_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_subscription_sku_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    @NotNull
    @ManyToOne(targetEntity = AccountSubscriptionEntity.class)
    @JoinColumn(name = "subscription", nullable = false)
    @Getter
    @Setter
    private AccountSubscriptionEntity subscription;

    @NotNull
    @Column(name = "`purchased_rows`")
    @Getter
    @Setter
    private Integer purchasedRows = 0;

    @NotNull
    @Column(name = "`purchased_calls`")
    @Getter
    @Setter
    private Integer purchasedCalls = 0;

    @NotNull
    @Column(name = "`used_rows`")
    @Getter
    private Integer usedRows = 0;

    @NotNull
    @Column(name = "`used_calls`")
    @Getter
    private Integer usedCalls = 0;

    @Transient
    public Integer getAvailableRows() {
        return this.purchasedRows - this.usedRows;
    }

    @Transient
    public Integer getAvailableCalls() {
        return this.purchasedCalls - this.usedCalls;
    }

    public void usePrepaidRows(int rows) {
        this.usedRows += rows;

        Assert.isTrue(this.purchasedRows >= this.usedRows, "Used rows exceed purchased rows");
    }

    public void usePrepaidCalls(int calls) {
        this.usedCalls += calls;

        Assert.isTrue(this.purchasedCalls >= this.usedCalls, "Used calls exceed purchased rows");
    }

    public AccountSubscriptionSkuDto toDto() {
        final AccountSubscriptionSkuDto s = new AccountSubscriptionSkuDto();

        s.setId(id);
        s.setOrderId(this.getOrder().getId());
        s.setPurchasedCalls(purchasedCalls);
        s.setPurchasedRows(purchasedRows);
        s.setSubscriptionId(this.getSubscription().getId());
        s.setUsedCalls(usedCalls);
        s.setUsedRows(usedRows);

        return s;
    }

}
