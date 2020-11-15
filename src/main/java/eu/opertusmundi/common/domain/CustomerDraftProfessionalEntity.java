package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.model.dto.CustomerDraftProfessionalDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.EnumCustomerType;
import eu.opertusmundi.common.model.dto.EnumLegalPersonType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerDraftProfesional")
@Table(schema = "web", name = "`customer_draft_professional`")
@DiscriminatorValue(value = "2")
public class CustomerDraftProfessionalEntity extends CustomerDraftEntity {

    protected CustomerDraftProfessionalEntity() {
        super(EnumCustomerType.PROFESSIONAL);
    }

    protected CustomerDraftProfessionalEntity(CustomerProfessionalEntity e, ProviderProfessionalCommandDto c) {
        super(EnumCustomerType.PROFESSIONAL);

        this.additionalInfo        = c.getAdditionalInfo();
        this.bankAccount           = BankAccountEmbeddable.from(c.getBankAccount());
        this.companyNumber         = c.getCompanyNumber();
        this.companyType           = c.getCompanyType();
        this.email                 = c.getEmail();
        this.headquartersAddress   = AddressEmbeddable.from(c.getHeadquartersAddress());
        this.legalPersonType       = c.getLegalPersonType();
        this.legalRepresentative   = CustomerRrepresentativeEmbeddable.from(c.getLegalRepresentative());
        this.logoImage             = c.getLogoImage();
        this.logoImageMimeType     = c.getLogoImageMimeType();
        this.name                  = c.getName();
        this.phone                 = c.getPhone();
        this.siteUrl               = c.getSiteUrl();

        this.createdAt       = ZonedDateTime.now();
        this.modifiedAt      = this.createdAt;

        // Merge existing object. These properties are set only during the first
        // successful registration with the payment service
        if (e != null) {
            this.bankAccount.setId(e.getBankAccount().getId());
            this.paymentProviderUser   = e.paymentProviderUser;
            this.paymentProviderWallet = e.paymentProviderWallet;
        }
    }

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "`headquarters_address_line1`")),
        @AttributeOverride(name = "line2", column = @Column(name = "`headquarters_address_line2`")),
        @AttributeOverride(name = "city", column = @Column(name = "`headquarters_address_city`")),
        @AttributeOverride(name = "region", column = @Column(name = "`headquarters_address_region`")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "`headquarters_address_postal_code`")),
        @AttributeOverride(name = "country", column = @Column(name = "`headquarters_address_country`")),
    })
    @Getter
    @Setter
    private AddressEmbeddable headquartersAddress;

    @Column(name = "`legal_person_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumLegalPersonType legalPersonType;

    @Column(name = "`name`")
    @Getter
    @Setter
    private String name;


    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address.line1",      column = @Column(name = "`legal_representative_address_line1`")),
        @AttributeOverride(name = "address.line2",      column = @Column(name = "`legal_representative_address_line2`")),
        @AttributeOverride(name = "address.city",       column = @Column(name = "`legal_representative_address_city`")),
        @AttributeOverride(name = "address.region",     column = @Column(name = "`legal_representative_address_region`")),
        @AttributeOverride(name = "address.postalCode", column = @Column(name = "`legal_representative_address_postal_code`")),
        @AttributeOverride(name = "address.country",    column = @Column(name = "`legal_representative_address_country`")),
        @AttributeOverride(name = "birthdate",          column = @Column(name = "`legal_representative_birthdate`")),
        @AttributeOverride(name = "countryOfResidence", column = @Column(name = "`legal_representative_country_of_residence`")),
        @AttributeOverride(name = "nationality",        column = @Column(name = "`legal_representative_nationality`")),
        @AttributeOverride(name = "email",              column = @Column(name = "`legal_representative_email`")),
        @AttributeOverride(name = "firstName",          column = @Column(name = "`legal_representative_first_name`")),
        @AttributeOverride(name = "lastName",           column = @Column(name = "`legal_representative_last_name`")),
    })
    @Getter
    @Setter
    private CustomerRrepresentativeEmbeddable legalRepresentative;

    @Column(name = "`company_number`")
    @Getter
    @Setter
    private String companyNumber;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id",                      column = @Column(name = "`payment_provider_bank_account`")),
        @AttributeOverride(name = "ownerName",               column = @Column(name = "`bank_account_owner_name`")),
        @AttributeOverride(name = "ownerAddress.line1",      column = @Column(name = "`bank_account_owner_address_line1`")),
        @AttributeOverride(name = "ownerAddress.line2",      column = @Column(name = "`bank_account_owner_address_line2`")),
        @AttributeOverride(name = "ownerAddress.city",       column = @Column(name = "`bank_account_owner_address_city`")),
        @AttributeOverride(name = "ownerAddress.region",     column = @Column(name = "`bank_account_owner_address_region`")),
        @AttributeOverride(name = "ownerAddress.postalCode", column = @Column(name = "`bank_account_owner_address_postal_code`")),
        @AttributeOverride(name = "ownerAddress.country",    column = @Column(name = "`bank_account_owner_address_country`")),
        @AttributeOverride(name = "iban",                    column = @Column(name = "`bank_account_iban`")),
        @AttributeOverride(name = "bic",                     column = @Column(name = "`bank_account_bic`")),
        @AttributeOverride(name = "tag",                     column = @Column(name = "`bank_account_tag`")),
    })
    @Getter
    @Setter
    private BankAccountEmbeddable bankAccount;

    @Column(name = "`additional_info`")
    @Getter
    @Setter
    private String additionalInfo;

    @Column(name = "`company_type`", length = 80)
    @Getter
    @Setter
    private String companyType;

    @Column(name = "`logo_image_binary`")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] logoImage;

    @Column(name = "`logo_image_mime_type`")
    @Getter
    @Setter
    private String logoImageMimeType;

    @Column(name = "`phone`", length = 20)
    @Getter
    @Setter
    private String phone;

    @Column(name = "`site_url`", length = 80)
    @Getter
    @Setter
    private String siteUrl;

    @Override
    public void update(CustomerCommandDto command) {
        final ProviderProfessionalCommandDto p = (ProviderProfessionalCommandDto) command;

        this.additionalInfo    = p.getAdditionalInfo();
        this.companyNumber     = p.getCompanyNumber();
        this.companyType       = p.getCompanyType();
        this.email             = p.getEmail();
        this.legalPersonType   = p.getLegalPersonType();
        this.logoImage         = p.getLogoImage();
        this.logoImageMimeType = p.getLogoImageMimeType();
        this.modifiedAt        = ZonedDateTime.now();
        this.name              = p.getName();
        this.phone             = p.getPhone();
        this.siteUrl           = p.getSiteUrl();

        if (p.getBankAccount() != null) {
            this.bankAccount = BankAccountEmbeddable.from(p.getBankAccount());
        }

        if (p.getHeadquartersAddress() != null) {
            this.headquartersAddress = AddressEmbeddable.from(p.getHeadquartersAddress());
        }

        if (p.getLegalRepresentative() != null) {
            this.legalRepresentative = CustomerRrepresentativeEmbeddable.from(p.getLegalRepresentative());
        }
    }

    @Override
    public CustomerDraftProfessionalDto toDto() {
        final CustomerDraftProfessionalDto p = new CustomerDraftProfessionalDto();

        p.setAdditionalInfo(this.additionalInfo);
        if (this.bankAccount != null) {
            p.setBankAccount(this.bankAccount.toDto());
        }
        p.setBankAccountIdempotentKey(this.bankAccountIdempotentKey);
        p.setCompanyNumber(this.companyNumber);
        p.setCompanyType(this.companyType);
        p.setCreatedAt(this.createdAt);
        p.setEmail(this.email);
        if (this.headquartersAddress != null) {
            p.setHeadquartersAddress(this.headquartersAddress.toDto());
        }
        p.setId(this.id);
        p.setKey(this.key);
        p.setLegalPersonType(this.legalPersonType);
        p.setLogoImage(this.logoImage);
        p.setLogoImageMimeType(this.logoImageMimeType);
        p.setModifiedAt(this.modifiedAt);
        p.setName(this.name);
        p.setPhone(this.phone);
        p.setRepresentative(this.legalRepresentative.toDto());
        p.setSiteUrl(this.siteUrl);
        p.setStatus(this.status);
        p.setType(this.type);
        p.setUserIdempotentKey(this.userIdempotentKey);
        p.setWalletIdempotentKey(this.walletIdempotentKey);

        return p;
    }

}
