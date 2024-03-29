package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.account.ConsumerDto;
import eu.opertusmundi.common.model.account.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.account.CustomerCommandDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.EnumKycLevel;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.converter.EnumCustomerTypeAttributeConverter;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Customer")
@Table(schema = "web", name = "`customer`")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "`type`", discriminatorType = DiscriminatorType.INTEGER)
public abstract class CustomerEntity {

    protected CustomerEntity() {
    }

    protected CustomerEntity(EnumMangopayUserType type) {
        this.type = type;
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.customer_id_seq", name = "customer_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "customer_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    protected Integer id;

    @NotNull
    @NaturalId
    @Column(name = "`draft_key`", columnDefinition = "uuid")
    @Getter
    @Setter
    protected UUID draftKey;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_customer_account"))
    @Getter
    @Setter
    protected AccountEntity account;

    @OneToMany(
        mappedBy = "customer",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    private List<CustomerKycLevelEntity> levelHistory = new ArrayList<>();

    @NotNull
    @Column(name = "`type`", nullable = false, updatable = false)
    @Convert(converter = EnumCustomerTypeAttributeConverter.class)
    @Getter
    protected EnumMangopayUserType type;

    @NotNull
    @Column(name = "`payment_provider_user`")
    @Getter
    @Setter
    protected String paymentProviderUser;

    @NotNull
    @Column(name = "`payment_provider_wallet`")
    @Getter
    @Setter
    protected String paymentProviderWallet;

    @NotNull
    @Column(name = "`kyc_level`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumKycLevel kycLevel;

    @Column(name = "`contract`", columnDefinition = "uuid")
    @Getter
    @Setter
    protected UUID contract;

    @NotNull
    @Email
    @Column(name = "`email`")
    @Getter
    @Setter
    protected String email;

    @NotNull
    @Column(name = "`email_verified`")
    @Getter
    @Setter
    protected boolean emailVerified = false;

    @Column(name = "`email_verified_at`")
    @Getter
    @Setter
    protected ZonedDateTime emailVerifiedAt;

    @NotNull
    @Column(name = "`created_at`")
    @Getter
    @Setter
    protected ZonedDateTime createdAt;

    @NotNull
    @Column(name = "`modified_at`")
    @Getter
    @Setter
    protected ZonedDateTime modifiedAt;

    @NotNull
    @Column(name = "`terms_accepted`")
    @Getter
    @Setter
    protected boolean termsAccepted = false;

    @Column(name = "`terms_accepted_at`")
    @Getter
    @Setter
    protected ZonedDateTime termsAcceptedAt;

    @NotNull
    @Column(name = "`wallet_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal walletFunds = BigDecimal.ZERO;

    @Column(name = "`wallet_funds_updated_on`")
    @Getter
    @Setter
    protected ZonedDateTime walletFundsUpdatedOn;

    @Column(name = "`blocked_inflows`")
    @Getter
    @Setter
    protected boolean blockedInflows = false;

    @Column(name = "`blocked_outflows`")
    @Getter
    @Setter
    protected boolean blockedOutflows = false;

    public abstract CustomerDto toDto();

    public abstract CustomerDto toDto(boolean includeHelpdeskDetails);

    public abstract ConsumerDto toConsumerDto();

    public abstract void update(CustomerCommandDto command);

    public abstract void update(CustomerDraftEntity e);

    public static CustomerEntity from(CustomerCommandDto command) {
        switch (command.getType()) {
            case INDIVIDUAL :
                final ConsumerIndividualCommandDto i = (ConsumerIndividualCommandDto) command;

                return new CustomerIndividualEntity(i);
            case PROFESSIONAL :
                final ProviderProfessionalCommandDto p = (ProviderProfessionalCommandDto) command;

                return new CustomerProfessionalEntity(p);
            default :
                return null;
        }
    }

    public static CustomerEntity from(CustomerDraftEntity e) {
        switch (e.getType()) {
            case INDIVIDUAL :
                final CustomerDraftIndividualEntity i = (CustomerDraftIndividualEntity) e;

                return new CustomerIndividualEntity(i);
            case PROFESSIONAL :
                final CustomerDraftProfessionalEntity p = (CustomerDraftProfessionalEntity) e;

                return new CustomerProfessionalEntity(p);
            default :
                return null;
        }
    }

}
