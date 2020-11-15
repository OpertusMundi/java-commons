package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.converter.EnumCustomerTypeAttributeConverter;
import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.model.dto.CustomerDraftDto;
import eu.opertusmundi.common.model.dto.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.EnumCustomerType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerDraft")
@Table(schema = "web", name = "`customer_draft`")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "`type`", discriminatorType = DiscriminatorType.INTEGER)
public abstract class CustomerDraftEntity {

    protected CustomerDraftEntity() {
    }

    protected CustomerDraftEntity(EnumCustomerType type) {
        this.type = type;
    }

    @Id
    @Column(name = "`id`")
    @SequenceGenerator(sequenceName = "web.customer_draft_id_seq", name = "customer_draft_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "customer_draft_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    protected Integer id;

    @NotNull
    @NaturalId
    @Column(name = "`key`", updatable = false, columnDefinition = "uuid")
    @Getter
    protected final UUID key = UUID.randomUUID();

    @NotNull
    @NaturalId
    @Column(name = "`idk_user`", updatable = false, columnDefinition = "uuid")
    @Getter
    protected final UUID userIdempotentKey = UUID.randomUUID();

    @NotNull
    @NaturalId
    @Column(name = "`idk_wallet`", updatable = false, columnDefinition = "uuid")
    @Getter
    protected final UUID walletIdempotentKey = UUID.randomUUID();

    @NotNull
    @NaturalId
    @Column(name = "`idk_account`", updatable = false, columnDefinition = "uuid")
    @Getter
    protected final UUID bankAccountIdempotentKey = UUID.randomUUID();

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_customer_draft_account"))
    @Getter
    @Setter
    protected AccountEntity account;

    @NotNull
    @Column(name = "`type`", nullable = false, insertable = false, updatable = false)
    @Convert(converter = EnumCustomerTypeAttributeConverter.class)
    @Getter
    protected EnumCustomerType type;

    @Column(name = "`payment_provider_user`")
    @Getter
    @Setter
    protected String paymentProviderUser;

    @Column(name = "`payment_provider_wallet`")
    @Getter
    @Setter
    protected String paymentProviderWallet;

    @Email
    @Column(name = "`email`")
    @Getter
    @Setter
    protected String email;

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
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumCustomerRegistrationStatus status = EnumCustomerRegistrationStatus.DRAFT;

    @Transient
    public boolean isProcessed() {
        return this.status == EnumCustomerRegistrationStatus.CANCELLED ||
               this.status == EnumCustomerRegistrationStatus.COMPLETED;
    }

    public abstract CustomerDraftDto toDto();

    public abstract void update(CustomerCommandDto command);

    public static CustomerDraftEntity from(CustomerEntity current, CustomerCommandDto command) {
        switch (command.getType()) {
            case INDIVIDUAL : {
                final CustomerIndividualEntity     e = (CustomerIndividualEntity) current;
                final ConsumerIndividualCommandDto c = (ConsumerIndividualCommandDto) command;

                return new CustomerDraftIndividualEntity(e, c);
            }
            case PROFESSIONAL : {
                final CustomerProfessionalEntity     e = (CustomerProfessionalEntity) current;
                final ProviderProfessionalCommandDto c = (ProviderProfessionalCommandDto) command;

                return new CustomerDraftProfessionalEntity(e, c);
            }
            default :
                return null;
        }
    }

}
