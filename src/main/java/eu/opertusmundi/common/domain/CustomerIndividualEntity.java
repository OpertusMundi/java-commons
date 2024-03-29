package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import eu.opertusmundi.common.model.account.ConsumerDto;
import eu.opertusmundi.common.model.account.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.account.CustomerCommandDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerIndividualDto;
import eu.opertusmundi.common.model.account.EnumKycLevel;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerIndividual")
@Table(schema = "web", name = "`customer_individual`")
@DiscriminatorValue(value = "1")
public class CustomerIndividualEntity extends CustomerEntity {

    protected CustomerIndividualEntity() {
        super(EnumMangopayUserType.INDIVIDUAL);
    }

    protected CustomerIndividualEntity(ConsumerIndividualCommandDto c) {
        super(EnumMangopayUserType.INDIVIDUAL);

        if (c.getAddress() != null) {
            this.address = AddressEmbeddable.from(c.getAddress());
        }
        this.birthdate             = c.getBirthdate();
        this.contract              = c.getContract();
        this.countryOfResidence    = c.getCountryOfResidence();
        this.email                 = c.getEmail();
        this.emailVerified         = false;
        this.firstName             = c.getFirstName();
        this.kycLevel              = EnumKycLevel.LIGHT;
        this.lastName              = c.getLastName();
        this.nationality           = c.getNationality();
        this.occupation            = c.getOccupation();
        this.paymentProviderUser   = c.getPaymentProviderUser();
        this.paymentProviderWallet = c.getPaymentProviderWallet();
        this.termsAccepted         = true;

        this.createdAt       = ZonedDateTime.now();
        this.modifiedAt      = this.createdAt;
        this.termsAcceptedAt = this.createdAt;
    }

    protected CustomerIndividualEntity(CustomerDraftIndividualEntity e) {
        super(EnumMangopayUserType.INDIVIDUAL);

        if (e.getAddress() != null) {
            this.address = e.getAddress().clone();
        }
        this.birthdate             = e.getBirthdate();
        this.contract              = null;
        this.countryOfResidence    = e.getCountryOfResidence();
        this.email                 = e.getEmail();
        this.emailVerified         = false;
        this.firstName             = e.getFirstName();
        this.draftKey              = e.getKey();
        this.kycLevel              = EnumKycLevel.LIGHT;
        this.lastName              = e.getLastName();
        this.nationality           = e.getNationality();
        this.occupation            = e.getOccupation();
        this.paymentProviderUser   = e.getPaymentProviderUser();
        this.paymentProviderWallet = e.getPaymentProviderWallet();
        this.termsAccepted         = true;

        this.createdAt       = ZonedDateTime.now();
        this.modifiedAt      = this.createdAt;
        this.termsAcceptedAt = this.createdAt;
    }

    @NotNull
    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    private String firstName;

    @NotNull
    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    private String lastName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1",      column = @Column(name = "`address_line1`",       nullable = false)),
        @AttributeOverride(name = "line2",      column = @Column(name = "`address_line2`")),
        @AttributeOverride(name = "city",       column = @Column(name = "`address_city`",        nullable = false)),
        @AttributeOverride(name = "region",     column = @Column(name = "`address_region`")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "`address_postal_code`", nullable = false)),
        @AttributeOverride(name = "country",    column = @Column(name = "`address_country`",     nullable = false)),
    })
    @Getter
    @Setter
    private AddressEmbeddable address;

    @Column(name = "`birthdate`")
    @Getter
    @Setter
    private ZonedDateTime birthdate;

    @Column(name = "`nationality`")
    @Getter
    @Setter
    private String nationality;

    @Column(name = "`country_of_residence`")
    @Getter
    @Setter
    private String countryOfResidence;

    @Column(name = "`occupation`")
    @Getter
    @Setter
    private String occupation;

    @Transient
    public String getFullName() {
        return StringUtils.joinWith(" ", lastName, firstName);
    }

    @Override
    public void update(CustomerDraftEntity e) {
        final CustomerDraftIndividualEntity i = (CustomerDraftIndividualEntity) e;

        if (i.getAddress() != null) {
            this.address = i.getAddress().clone();
        }
        this.birthdate          = i.getBirthdate();
        this.contract           = null;
        this.countryOfResidence = i.getCountryOfResidence();
        this.firstName          = i.getFirstName();
        this.lastName           = i.getLastName();
        this.modifiedAt         = ZonedDateTime.now();
        this.nationality        = i.getNationality();
        this.occupation         = i.getOccupation();

        if (!StringUtils.isBlank(i.getEmail()) && !i.getEmail().equals(this.email)) {
            this.email           = i.getEmail();
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }
    }

    @Override
    public void update(CustomerCommandDto command) {
        final ConsumerIndividualCommandDto c = (ConsumerIndividualCommandDto) command;

        if (c.getAddress() != null) {
            this.address = AddressEmbeddable.from(c.getAddress());
        }
        this.birthdate          = c.getBirthdate();
        this.contract           = c.getContract();
        this.countryOfResidence = c.getCountryOfResidence();
        this.firstName          = c.getFirstName();
        this.lastName           = c.getLastName();
        this.modifiedAt         = ZonedDateTime.now();
        this.nationality        = c.getNationality();
        this.occupation         = c.getOccupation();

        if (!StringUtils.isBlank(c.getEmail()) && !c.getEmail().equals(this.email)) {
            this.email           = c.getEmail();
            this.emailVerified   = false;
            this.emailVerifiedAt = null;
        }
    }

    @Override
    public CustomerDto toDto() {
        return this.toDto(false);
    }

    @Override
    public CustomerDto toDto(boolean includeHelpdeskDetails) {
        final CustomerIndividualDto c = new CustomerIndividualDto();

        if (this.address != null) {
            c.setAddress(this.address.toDto());
        }
        c.setBirthdate(this.birthdate);
        c.setBlockedInflows(this.blockedInflows);
        c.setBlockedOutflows(this.blockedOutflows);
        c.setContract(this.contract);
        c.setCountryOfResidence(this.countryOfResidence);
        c.setCreatedAt(this.createdAt);
        c.setEmail(this.email);
        c.setFirstName(this.firstName);
        c.setId(this.id);
        c.setKey(this.getAccount().getKey());
        c.setKycLevel(this.kycLevel);
        c.setLastName(this.lastName);
        c.setModifiedAt(this.modifiedAt);
        c.setNationality(this.nationality);
        c.setOccupation(this.occupation);
        c.setPaymentProviderUser(this.paymentProviderUser);
        c.setPaymentProviderWallet(this.paymentProviderWallet);
        c.setTermsAccepted(this.termsAccepted);
        c.setTermsAcceptedAt(this.termsAcceptedAt);
        c.setType(this.type);
        c.setWalletFunds(this.walletFunds);
        c.setWalletFundsUpdatedOn(this.walletFundsUpdatedOn);

        return c;
    }

    @Override
    public ConsumerDto toConsumerDto() {
        final ConsumerDto c = new ConsumerDto();

        c.setCountry(countryOfResidence);
        c.setId(account.getId());
        c.setKey(account.getKey());
        c.setName(this.getFullName());

        return c;
    }

}
