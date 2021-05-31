package eu.opertusmundi.common.model.analytics;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerIndividualDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumKycLevel;
import eu.opertusmundi.common.model.account.EnumLegalPersonType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProfileRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID     key;
    private Profile  profile;
    private Customer consumer;
    private Customer provider;

    public static ProfileRecord from(AccountDto a) {
        final ProfileRecord r = new ProfileRecord();

        r.key     = a.getKey();
        r.profile = Profile.from(a);

        if (a.getProfile() != null) {
            r.consumer = Customer.from(a.getProfile().getConsumer().getCurrent());
            r.provider = Customer.from(a.getProfile().getProvider().getCurrent());
        }

        return r;
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    @Setter
    public static class Profile implements Serializable {

        private static final long serialVersionUID = 1L;

        private ZonedDateTime        activatedAt;
        private EnumActivationStatus activationStatus;
        private boolean              emailVerified;
        private ZonedDateTime        emailVerifiedAt;
        private EnumAuthProvider     idpName;
        private ZonedDateTime        registeredAt;
        private Set<EnumRole>        roles;
        private String               locale;

        public static Profile from(AccountDto a) {
            final Profile p = new Profile();

            p.activatedAt      = a.getActivatedAt();
            p.activationStatus = a.getActivationStatus();
            p.emailVerified    = a.isEmailVerified();
            p.emailVerifiedAt  = a.getEmailVerifiedAt();
            p.idpName          = a.getIdpName();
            p.locale           = a.getProfile().getLocale();
            p.registeredAt     = a.getRegisteredAt();
            p.roles            = a.getRoles();

            return p;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    @Setter
    public static class Customer implements Serializable {

        private static final long serialVersionUID = 1L;

        private ZonedDateTime        createdAt;
        private EnumKycLevel         kycLevel;
        private ZonedDateTime        modifiedAt;
        private boolean              termsAccepted;
        private ZonedDateTime        termsAcceptedAt;
        private EnumMangopayUserType type;

        private String countryOfResidence;
        private String nationality;
        private String occupation;

        private String              additionalInfo;
        private String              companyType;
        private EnumLegalPersonType legalPersonType;
        private String              name;
        private String              siteUrl;

        protected Customer(CustomerDto c) {
            createdAt       = c.getCreatedAt();
            kycLevel        = c.getKycLevel();
            modifiedAt      = c.getModifiedAt();
            termsAccepted   = c.isTermsAccepted();
            termsAcceptedAt = c.getTermsAcceptedAt();
            type            = c.getType();
        }

        public static Customer from(CustomerDto c) {
            if (c == null) {
                return null;
            }

            switch (c.getType()) {
                case INDIVIDUAL :
                    return Customer.fromIndividual((CustomerIndividualDto) c);
                case PROFESSIONAL :
                    return Customer.fromProfessional((CustomerProfessionalDto) c);
                default :
                    return null;
            }
        }

        private static Customer fromIndividual(CustomerIndividualDto c) {
            if (c == null) {
                return null;
            }
            final Customer r = new Customer(c);

            r.countryOfResidence = c.getCountryOfResidence();
            r.nationality        = c.getNationality();
            r.occupation         = c.getOccupation();

            return r;
        }

        private static Customer fromProfessional(CustomerProfessionalDto c) {
            if (c == null) {
                return null;
            }
            final Customer r = new Customer(c);

            r.additionalInfo  = c.getAdditionalInfo();
            r.companyType     = c.getCompanyType();
            r.legalPersonType = c.getLegalPersonType();
            r.name            = c.getName();
            r.siteUrl         = c.getSiteUrl();

            return r;
        }
    }

}
