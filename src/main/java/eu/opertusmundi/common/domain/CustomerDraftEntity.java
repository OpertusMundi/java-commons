package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.List;
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.account.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.account.ConsumerProfessionalCommandDto;
import eu.opertusmundi.common.model.account.CustomerCommandDto;
import eu.opertusmundi.common.model.account.CustomerDraftDto;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.converter.EnumCustomerTypeAttributeConverter;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerDraft")
@Table(schema = "web", name = "`customer_draft`")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "`type`", discriminatorType = DiscriminatorType.INTEGER)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public abstract class CustomerDraftEntity {

    protected CustomerDraftEntity() {
    }

    protected CustomerDraftEntity(EnumMangopayUserType type) {
        this.type = type;

        this.resetIdempotencyKeys();
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
    @Column(name = "`idk_user`", columnDefinition = "uuid")
    @Getter
    protected UUID userIdempotentKey;

    @NotNull
    @Column(name = "`idk_wallet`", columnDefinition = "uuid")
    @Getter
    protected UUID walletIdempotentKey;

    @NotNull
    @Column(name = "`idk_account`", columnDefinition = "uuid")
    @Getter
    protected UUID bankAccountIdempotentKey;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_customer_draft_account"))
    @Getter
    @Setter
    protected AccountEntity account;

    @NotNull
    @Column(name = "`type`", nullable = false, updatable = false)
    @Convert(converter = EnumCustomerTypeAttributeConverter.class)
    @Getter
    protected EnumMangopayUserType type;

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

    @Column(name = "`workflow_error_details`")
    @Getter
    @Setter
    protected String workflowErrorDetails;

    @Type(type = "jsonb")
    @Column(name = "`workflow_error_messages`", columnDefinition = "jsonb")
    @Getter
    @Setter
    protected List<Message> workflowErrorMessages;

    @Column(name = "`helpdesk_error_message`")
    @Getter
    @Setter
    protected String helpdeskErrorMessage;

    @Transient
    public boolean isProcessed() {
        return this.status == EnumCustomerRegistrationStatus.CANCELLED ||
               this.status == EnumCustomerRegistrationStatus.COMPLETED;
    }

    public CustomerDraftDto toDto() {
        return this.toDto(false);
    }

    public abstract CustomerDraftDto toDto(boolean includeHelpdeskDetails);

    public abstract void update(CustomerCommandDto command);

    public static CustomerDraftEntity consumerOf(CustomerEntity current, CustomerCommandDto command) {
        switch (command.getType()) {
            case INDIVIDUAL : {
                final CustomerIndividualEntity     e = (CustomerIndividualEntity) current;
                final ConsumerIndividualCommandDto c = (ConsumerIndividualCommandDto) command;

                return new CustomerDraftIndividualEntity(e, c);
            }
            case PROFESSIONAL : {
                final CustomerProfessionalEntity     e = (CustomerProfessionalEntity) current;
                final ConsumerProfessionalCommandDto c = (ConsumerProfessionalCommandDto) command;

                return new CustomerDraftProfessionalEntity(e, c);
            }
            default :
                return null;
        }
    }

    public static CustomerDraftEntity providerOf(CustomerEntity current, ProviderProfessionalCommandDto command) {
        final CustomerProfessionalEntity e = (CustomerProfessionalEntity) current;

        return new CustomerDraftProfessionalEntity(e, command);
    }

    public void resetIdempotencyKeys() {
        this.userIdempotentKey        = UUID.randomUUID();
        this.walletIdempotentKey      = UUID.randomUUID();
        this.bankAccountIdempotentKey = UUID.randomUUID();
    }

}
