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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.dto.CustomerProfessionalDto;
import eu.opertusmundi.common.model.dto.EnumCustomerType;
import eu.opertusmundi.common.model.dto.EnumKycLevel;
import eu.opertusmundi.common.model.dto.EnumLegalPersonType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerProfesionalEntity")
@Table(schema = "web", name = "`customer_professional`")
@DiscriminatorValue(value = "2")
public class CustomerProfessionalEntity extends CustomerEntity {

    protected CustomerProfessionalEntity() {
        super(EnumCustomerType.PROFESSIONAL);
    }

    protected CustomerProfessionalEntity(ProviderProfessionalCommandDto c) {
        super(EnumCustomerType.PROFESSIONAL);

        this.additionalInfo        = c.getAdditionalInfo();
        this.bankAccount           = BankAccountEmbeddable.from(c.getBankAccount());
        this.companyNumber         = c.getCompanyNumber();
        this.companyType           = c.getCompanyType();
        this.contract              = c.getContract();
        this.email                 = c.getEmail();
        this.emailVerified         = false;
        this.headquartersAddress   = AddressEmbeddable.from(c.getHeadquartersAddress());
        this.kycLevel              = EnumKycLevel.LIGHT;
        this.legalPersonType       = c.getLegalPersonType();
        this.legalRepresentative   = CustomerRrepresentativeEmbeddable.from(c.getLegalRepresentative());
        this.logoImage             = c.getLogoImage();
        this.logoImageMimeType     = c.getLogoImageMimeType();
        this.name                  = c.getName();
        this.paymentProviderUser   = c.getPaymentProviderUser();
        this.paymentProviderWallet = c.getPaymentProviderWallet();
        this.phone                 = c.getPhone();
        this.ratingCount           = 0;
        this.ratingTotal           = 0;
        this.siteUrl               = c.getSiteUrl();
        this.termsAccepted         = true;

        this.createdAt       = ZonedDateTime.now();
        this.modifiedAt      = this.createdAt;
        this.termsAcceptedAt = this.createdAt;
    }

    protected CustomerProfessionalEntity(CustomerDraftProfessionalEntity e) {
        super(EnumCustomerType.PROFESSIONAL);

        this.additionalInfo        = e.getAdditionalInfo();
        this.bankAccount           = e.getBankAccount().clone();
        this.companyNumber         = e.getCompanyNumber();
        this.companyType           = e.getCompanyType();
        this.contract              = null;
        this.email                 = e.getEmail();
        this.emailVerified         = false;
        this.headquartersAddress   = e.getHeadquartersAddress().clone();
        this.kycLevel              = EnumKycLevel.LIGHT;
        this.legalPersonType       = e.getLegalPersonType();
        this.legalRepresentative   = e.getLegalRepresentative().clone();
        this.logoImage             = e.getLogoImage();
        this.logoImageMimeType     = e.getLogoImageMimeType();
        this.name                  = e.getName();
        this.paymentProviderUser   = e.getPaymentProviderUser();
        this.paymentProviderWallet = e.getPaymentProviderWallet();
        this.phone                 = e.getPhone();
        this.ratingCount           = 0;
        this.ratingTotal           = 0;
        this.siteUrl               = e.getSiteUrl();
        this.termsAccepted         = true;

        this.createdAt       = ZonedDateTime.now();
        this.modifiedAt      = this.createdAt;
        this.termsAcceptedAt = this.createdAt;
    }

    @NotNull
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "line1", column = @Column(name = "`headquarters_address_line1`", nullable = false)),
            @AttributeOverride(name = "line2", column = @Column(name = "`headquarters_address_line2`")),
            @AttributeOverride(name = "city", column = @Column(name = "`headquarters_address_city`", nullable = false)),
            @AttributeOverride(name = "region", column = @Column(name = "`headquarters_address_region`", nullable = false)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "`headquarters_address_postal_code`", nullable = false)),
            @AttributeOverride(name = "country", column = @Column(name = "`headquarters_address_country`", nullable = false)),})
    @Getter
    @Setter
    private AddressEmbeddable headquartersAddress;

    @NotNull
    @Column(name = "`legal_person_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumLegalPersonType legalPersonType;

    @NotNull
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

    @NotNull
    @Column(name = "`company_number`")
    @Getter
    @Setter
    private String companyNumber;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id",                      column = @Column(name = "`payment_provider_bank_account`")),
        @AttributeOverride(name = "ownerName",               column = @Column(name = "`bank_account_owner_name`",                nullable = false)),
        @AttributeOverride(name = "ownerAddress.line1",      column = @Column(name = "`bank_account_owner_address_line1`",       nullable = false)),
        @AttributeOverride(name = "ownerAddress.line2",      column = @Column(name = "`bank_account_owner_address_line2`")),
        @AttributeOverride(name = "ownerAddress.city",       column = @Column(name = "`bank_account_owner_address_city`",        nullable = false)),
        @AttributeOverride(name = "ownerAddress.region",     column = @Column(name = "`bank_account_owner_address_region`")),
        @AttributeOverride(name = "ownerAddress.postalCode", column = @Column(name = "`bank_account_owner_address_postal_code`", nullable = false)),
        @AttributeOverride(name = "ownerAddress.country",    column = @Column(name = "`bank_account_owner_address_country`",     nullable = false)),
        @AttributeOverride(name = "iban",                    column = @Column(name = "`bank_account_iban`",                      nullable = false)),
        @AttributeOverride(name = "bic",                     column = @Column(name = "`bank_account_bic`",                       nullable = false)),
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

    @NotNull
    @Column
    @Getter
    @Setter
    private Integer ratingCount = 0;

    @NotNull
    @Column
    @Getter
    @Setter
    private Integer ratingTotal = 0;

    @Column(name = "`site_url`", length = 80)
    @Getter
    @Setter
    private String siteUrl;

    @Transient
    public Double getRating() {
        if (this.ratingCount == 0) {
            return null;
        }
        final double rating = (double) this.ratingTotal / (double) this.ratingCount;

        return Math.round(rating * 10) / 10.0;
    }

    @Override
    public void update(CustomerDraftEntity e) {
        final CustomerDraftProfessionalEntity p = (CustomerDraftProfessionalEntity) e;

        this.additionalInfo      = p.getAdditionalInfo();
        this.companyNumber       = p.getCompanyNumber();
        this.companyType         = p.getCompanyType();
        this.contract            = null;
        this.legalPersonType     = p.getLegalPersonType();
        this.logoImage           = p.getLogoImage();
        this.logoImageMimeType   = p.getLogoImageMimeType();
        this.modifiedAt          = ZonedDateTime.now();
        this.name                = p.getName();
        this.phone               = p.getPhone();
        this.legalRepresentative = p.getLegalRepresentative().clone();
        this.siteUrl             = p.getSiteUrl();

        if (p.getBankAccount() != null) {
            this.bankAccount = p.getBankAccount().clone();
        }

        if (p.getHeadquartersAddress() != null) {
            this.headquartersAddress = p.getHeadquartersAddress().clone();
        }

        if (!StringUtils.isBlank(p.getPaymentProviderUser())) {
            this.paymentProviderUser = p.getPaymentProviderUser();
        }

        if (!StringUtils.isBlank(p.getPaymentProviderWallet())) {
            this.paymentProviderWallet = p.getPaymentProviderWallet();
        }

        if (!StringUtils.isBlank(p.getEmail()) && !p.getEmail().equals(this.email)) {
            this.email           = p.getEmail();
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }
    }

    @Override
    public void update(CustomerCommandDto command) {
        final ProviderProfessionalCommandDto c = (ProviderProfessionalCommandDto) command;

        this.additionalInfo      = c.getAdditionalInfo();
        this.companyNumber       = c.getCompanyNumber();
        this.companyType         = c.getCompanyType();
        this.contract            = c.getContract();
        this.legalPersonType     = c.getLegalPersonType();
        this.logoImage           = c.getLogoImage();
        this.logoImageMimeType   = c.getLogoImageMimeType();
        this.modifiedAt          = ZonedDateTime.now();
        this.name                = c.getName();
        this.phone               = c.getPhone();
        this.legalRepresentative = CustomerRrepresentativeEmbeddable.from(c.getLegalRepresentative());
        this.siteUrl             = c.getSiteUrl();

        if (c.getBankAccount() != null) {
            this.bankAccount = BankAccountEmbeddable.from(c.getBankAccount());
        }

        if (c.getHeadquartersAddress() != null) {
            this.headquartersAddress = AddressEmbeddable.from(c.getHeadquartersAddress());
        }

        if (!StringUtils.isBlank(c.getPaymentProviderUser())) {
            this.paymentProviderUser = c.getPaymentProviderUser();
        }

        if (!StringUtils.isBlank(c.getPaymentProviderWallet())) {
            this.paymentProviderWallet = c.getPaymentProviderWallet();
        }

        if (!StringUtils.isBlank(c.getEmail()) && !c.getEmail().equals(this.email)) {
            this.email           = c.getEmail();
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }
    }

    @Override
    public CustomerProfessionalDto toDto() {
        final CustomerProfessionalDto p = new CustomerProfessionalDto();

        p.setAdditionalInfo(this.additionalInfo);
        if (this.bankAccount != null) {
            p.setBankAccount(this.bankAccount.toDto());
        }
        p.setCompanyNumber(this.companyNumber);
        p.setCompanyType(this.companyType);
        p.setContract(this.contract);
        p.setCreatedAt(this.createdAt);
        p.setEmail(this.email);
        if (this.headquartersAddress != null) {
            p.setHeadquartersAddress(this.headquartersAddress.toDto());
        }
        p.setId(this.id);
        p.setKycLevel(this.kycLevel);
        p.setLegalPersonType(this.legalPersonType);
        p.setLogoImage(this.logoImage);
        p.setLogoImageMimeType(this.logoImageMimeType);
        p.setModifiedAt(this.modifiedAt);
        p.setName(this.name);
        p.setPaymentProviderUser(this.paymentProviderUser);
        p.setPaymentProviderWallet(this.paymentProviderWallet);
        p.setPhone(this.phone);
        p.setRating(this.getRating());
        p.setRepresentative(this.legalRepresentative.toDto());
        p.setSiteUrl(this.siteUrl);
        p.setTermsAccepted(this.termsAccepted);
        p.setTermsAcceptedAt(this.termsAcceptedAt);
        p.setType(this.type);

        return p;
    }

    public static CustomerProfessionalEntity from(CustomerDraftProfessionalEntity r) {
        final CustomerProfessionalEntity p = new CustomerProfessionalEntity();

        p.additionalInfo        = r.getAdditionalInfo();
        p.bankAccount           = r.getBankAccount().clone();
        p.companyNumber         = r.getCompanyNumber();
        p.companyType           = r.getCompanyType();
        p.contract              = null;
        p.email                 = r.getEmail();
        p.emailVerified         = false;
        p.headquartersAddress   = r.getHeadquartersAddress().clone();
        p.draftKey              = r.getKey();
        p.kycLevel              = EnumKycLevel.LIGHT;
        p.legalPersonType       = r.getLegalPersonType();
        p.legalRepresentative   = r.getLegalRepresentative().clone();
        p.logoImage             = r.getLogoImage();
        p.logoImageMimeType     = r.getLogoImageMimeType();
        p.name                  = r.getName();
        p.paymentProviderUser   = r.getPaymentProviderUser();
        p.paymentProviderWallet = r.getPaymentProviderWallet();
        p.phone                 = r.getPhone();
        p.ratingCount           = 0;
        p.ratingTotal           = 0;
        p.siteUrl               = r.getSiteUrl();

        return p;
    }

}
