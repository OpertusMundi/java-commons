package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.model.dto.CustomerDraftDto;
import eu.opertusmundi.common.model.dto.CustomerDraftIndividualDto;
import eu.opertusmundi.common.model.dto.ConsumerIndividualCommandDto;
import eu.opertusmundi.common.model.dto.EnumCustomerType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CustomerDraftIndividual")
@Table(schema = "web", name = "`customer_draft_individual`")
@DiscriminatorValue(value = "1")
public class CustomerDraftIndividualEntity extends CustomerDraftEntity {

    protected CustomerDraftIndividualEntity() {
        super(EnumCustomerType.INDIVIDUAL);
    }

    protected CustomerDraftIndividualEntity(CustomerIndividualEntity e, ConsumerIndividualCommandDto c) {
        super(EnumCustomerType.INDIVIDUAL);

        this.address            = AddressEmbeddable.from(c.getAddress());
        this.birthdate          = c.getBirthdate();
        this.countryOfResidence = c.getCountryOfResidence();
        this.email              = c.getEmail();
        this.firstName          = c.getFirstName();
        this.lastName           = c.getLastName();
        this.nationality        = c.getNationality();
        this.occupation         = c.getOccupation();

        this.createdAt  = ZonedDateTime.now();
        this.modifiedAt = this.createdAt;

        // Merge existing object. These properties are set only during the first
        // successful registration with the payment service
        if (e != null) {
            this.paymentProviderUser   = e.paymentProviderUser;
            this.paymentProviderWallet = e.paymentProviderWallet;
        }
    }

    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    private String firstName;

    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    private String lastName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "`address_line1`")),
        @AttributeOverride(name = "line2", column = @Column(name = "`address_line2`")),
        @AttributeOverride(name = "city", column = @Column(name = "`address_city`")),
        @AttributeOverride(name = "region", column = @Column(name = "`address_region`")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "`address_postal_code`")),
        @AttributeOverride(name = "country", column = @Column(name = "`address_country`")),
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

    @Override
    public void update(CustomerCommandDto command) {
        final ConsumerIndividualCommandDto i = (ConsumerIndividualCommandDto) command;

        this.address            = AddressEmbeddable.from(i.getAddress());
        this.birthdate          = i.getBirthdate();
        this.countryOfResidence = i.getCountryOfResidence();
        this.email              = i.getEmail();
        this.firstName          = i.getFirstName();
        this.lastName           = i.getLastName();
        this.modifiedAt         = ZonedDateTime.now();
        this.nationality        = i.getNationality();
        this.occupation         = i.getOccupation();
    }

    @Override
    public CustomerDraftDto toDto() {
        final CustomerDraftIndividualDto c = new CustomerDraftIndividualDto();

        c.setAddress(this.address.toDto());
        c.setBankAccountIdempotentKey(this.bankAccountIdempotentKey);
        c.setBirthdate(this.birthdate);
        c.setCountryOfResidence(this.countryOfResidence);
        c.setCreatedAt(this.createdAt);
        c.setEmail(this.email);
        c.setFirstName(this.firstName);
        c.setId(this.id);
        c.setKey(this.key);
        c.setLastName(this.lastName);
        c.setModifiedAt(this.modifiedAt);
        c.setNationality(this.nationality);
        c.setOccupation(this.occupation);
        c.setStatus(this.status);
        c.setType(this.type);
        c.setUserIdempotentKey(this.userIdempotentKey);
        c.setWalletIdempotentKey(this.walletIdempotentKey);

        return c;
    }

}
