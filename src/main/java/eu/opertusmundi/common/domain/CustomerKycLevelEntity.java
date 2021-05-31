package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.EnumKycLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerKycLevel")
@Table(schema = "web", name = "`customer_kyc_level_hist`")
public class CustomerKycLevelEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.customer_kyc_level_hist_id_seq", name = "customer_kyc_level_hist_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "customer_kyc_level_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`customer`", nullable = false)
    @Getter
    @Setter
    private CustomerEntity customer;

    @NotNull
    @Column(name = "`level`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumKycLevel level;

    @NotNull
    @Column(name = "`updated_on`")
    @Getter
    @Setter
    private ZonedDateTime updatedOn;

}
