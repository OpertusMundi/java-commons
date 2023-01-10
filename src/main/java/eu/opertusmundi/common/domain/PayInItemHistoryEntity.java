package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInHistoryItem")
@Table(schema = "analytics", name = "`payin_item_hist`")
@Getter
@Setter
public class PayInItemHistoryEntity {

    @Id
    @Column(name = "`id`")
    private Integer id;

    @NotNull
    @Column(name = "`consumer`", updatable = false)
    private Integer consumer;

    @NotNull
    @Column(name = "`provider`", updatable = false)
    private Integer provider;

    @NotNull
    @NaturalId
    @Column(name = "provider_key", updatable = false, columnDefinition = "uuid")
    private UUID providerKey;

    @NotNull
    @Column(name = "asset_pid", updatable = false)
    private String assetId;

    @NotNull
    @Column(name = "asset_type", updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumOrderItemType assetType;

    @Column(name = "segment", updatable = false)
    @Enumerated(EnumType.STRING)
    private EnumTopicCategory segment;

    @NotNull
    @Column(name = "payin_op_id", updatable = false)
    private Integer payInId;

    @NotNull
    @Column(name = "payin_provider_id", updatable = false)
    private String payInProviderId;

    @NotNull
    @Column(name = "payin_executed_on", updatable = false)
    private ZonedDateTime payInExecutedOn;

    @NotNull
    @Column(name = "payin_year", updatable = false)
    private Integer payInYear;

    @NotNull
    @Column(name = "payin_month", updatable = false)
    private Integer payInMonth;

    @NotNull
    @Column(name = "payin_week", updatable = false)
    private Integer payInWeek;

    @NotNull
    @Column(name = "payin_day", updatable = false)
    private Integer payInDay;

    @NotNull
    @Column(name = "payin_country", updatable = false)
    private String payInCountry;

    @NotNull
    @Column(name = "`payin_total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal payInTotalPrice;

    @NotNull
    @Column(name = "`payin_total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal payInTotalPriceExcludingTax;

    @NotNull
    @Column(name = "`payin_total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal payInTotalTax;

    @Column(name = "transfer_provider_id")
    private String transferProviderId;

    @Column(name = "transfer_executed_on")
    private ZonedDateTime transferExecutedOn;

    @Column(name = "transfer_year")
    private Integer transferYear;

    @Column(name = "transfer_month")
    private Integer transferMonth;

    @Column(name = "transfer_week")
    private Integer transferWeek;

    @Column(name = "transfer_day")
    private Integer transferDay;

    @Column(name = "`transfer_credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal transferCreditedFunds;

    @Column(name = "`transfer_platform_fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal transferPlatformFees;

    @NotNull
    @Column(name = "refund")
    private boolean refunded;

}