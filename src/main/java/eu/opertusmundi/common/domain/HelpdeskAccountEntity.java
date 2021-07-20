package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.account.helpdesk.EnumHelpdeskRole;
import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountDto;
import eu.opertusmundi.common.model.account.helpdesk.HelpdeskProfileCommandDto;
import eu.opertusmundi.common.model.account.helpdesk.SimpleHelpdeskAccountDto;
import lombok.Getter;
import lombok.Setter;


@Entity(name = "HelpdeskAccount")
@Table(
    schema = "admin", name = "`account`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_key", columnNames = {"`key`"}),
        @UniqueConstraint(name = "uq_account_email", columnNames = {"`email`"}),
    }
)
public class HelpdeskAccountEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "`admin.account_id_seq`", name = "admin_account_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "admin_account_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    Integer id ;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @Column(name = "`active`")
    @Getter
    @Setter
    boolean active = true;

    @Column(name = "`blocked`")
    @Getter
    @Setter
    boolean blocked = false;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    String email;

    @Column(name = "`email_verified`")
    @Getter
    @Setter
    boolean emailVerified = false;

    @Column(name = "`email_verified_on`")
    @Getter
    @Setter
    ZonedDateTime emailVerifiedOn;

    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    String firstName;

    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    String lastName;

    @NotNull
    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`locale`")
    @Getter
    @Setter
    String locale;

    @Column(name = "`password`")
    @Getter
    @Setter
    String password;

    @Column(name = "`phone`", length = 15)
    @Getter
    @Setter
    private String phone;

    @Column(name = "`mobile`", length = 15)
    @Getter
    @Setter
    private String mobile;

    @Column(name = "image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] image;

    @Column(name = "image_mime_type", length = 30)
    @Getter
    @Setter
    private String imageMimeType;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    ZonedDateTime createdOn;

    @NotNull
    @Column(name = "`modified_on`")
    @Getter
    @Setter
    ZonedDateTime modifiedOn;

    @OneToMany(
        mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true
    )
    List<HelpdeskAccountRoleEntity> roles = new ArrayList<>();

    public HelpdeskAccountEntity() {

    }

    public HelpdeskAccountEntity(int id) {
        this.id = id;
    }

    public HelpdeskAccountEntity(String email) {
        this.email    = email;
    }

    public void setName(String firstname, String lastname) {
        this.firstName = firstname;
        this.lastName  = lastname;
    }

    public String getUserName() {
        return this.email;
    }

    public Set<EnumHelpdeskRole> getRoles() {
        final EnumSet<EnumHelpdeskRole> r = EnumSet.noneOf(EnumHelpdeskRole.class);
        for (final HelpdeskAccountRoleEntity ar: this.roles) {
            r.add(ar.role);
        }
        return r;
    }

    public boolean hasRole(EnumHelpdeskRole role) {
        for (final HelpdeskAccountRoleEntity ar: this.roles) {
            if (role == ar.role) {
                return true;
            }
        }
        return false;
    }

    public void grant(EnumHelpdeskRole role, HelpdeskAccountEntity grantedBy) {
        if (!this.hasRole(role)) {
            this.roles.add(new HelpdeskAccountRoleEntity(this, role, ZonedDateTime.now(), grantedBy));
        }
    }

    public void revoke(EnumHelpdeskRole role) {
        HelpdeskAccountRoleEntity target = null;
        for (final HelpdeskAccountRoleEntity ar: this.roles) {
            if (role == ar.role) {
                target = ar;
                break;
            }
        }
        if (target != null) {
            this.roles.remove(target);
        }
    }

    public void revokeAll() {
        this.roles.clear();
    }

    public void updateProfile(HelpdeskProfileCommandDto command) {
        this.firstName     = command.getFirstName();
        this.image         = command.getImage();
        this.imageMimeType = command.getImageMimeType();
        this.lastName      = command.getLastName();
        this.locale        = command.getLocale();
        this.mobile        = command.getMobile();
        this.modifiedOn    = ZonedDateTime.now();
        this.phone         = command.getPhone();
    }

	public HelpdeskAccountDto toDto() {
		final HelpdeskAccountDto a = new HelpdeskAccountDto();

		a.setActive(this.active);
		a.setBlocked(this.blocked);
        a.setCreatedOn(this.createdOn);
		a.setEmail(this.email);
		a.setEmailVerified(this.emailVerified);
		a.setEmailVerifiedOn(this.emailVerifiedOn);
		a.setFirstName(this.firstName);
		a.setId(this.id);
		a.setImage(this.image);
		a.setImageMimeType(this.imageMimeType);
		a.setKey(this.key);
		a.setLastName(this.lastName);
		a.setLocale(this.locale);
		a.setMobile(this.mobile);
		a.setModifiedOn(this.modifiedOn);
		a.setPhone(this.phone);
		a.setRoles(this.getRoles());

		return a;
	}
	
    public SimpleHelpdeskAccountDto toSimpleDto() {
        final SimpleHelpdeskAccountDto a = new SimpleHelpdeskAccountDto();

        a.setEmail(this.email);
        a.setFirstName(this.firstName);
        a.setId(this.id);
        a.setKey(this.key);
        a.setLastName(this.lastName);

        return a;
    }

}
