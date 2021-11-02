package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ActivationToken")
@Table(schema = "web", name = "`activation_token`")
public class ActivationTokenEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.activation_token_id_seq", name = "activation_token_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "activation_token_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.EAGER
    )
    @JoinColumn(name = "`account`", nullable = false, updatable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    private String email;

    @NotNull
    @Column(name = "`type`", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumActivationTokenType type;

    @NotNull
    @Column(name = "token", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID token = UUID.randomUUID();

    @Column(name = "`created_at`", updatable = false)
    @Getter
    private final ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "`redeemed_at`")
    @Getter
    @Setter
    private ZonedDateTime redeemedAt;

    @Column(name = "`valid`")
    @Getter
    @Setter
    boolean valid;

    @Column(name = "`duration`")
    @Getter
    @Setter
    private int duration;

    public boolean isExpired() {
        if (this.redeemedAt != null) {
            return true;
        }
        return this.createdAt.plusHours(this.duration).isBefore(ZonedDateTime.now());
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link ActivationTokenDto} instance
     */
    public ActivationTokenDto toDto() {
        final ActivationTokenDto o = new ActivationTokenDto();

        o.setAccount(this.account.getId());
        o.setCreatedAt(this.createdAt);
        o.setDuration(this.duration);
        o.setEmail(this.email);
        o.setExpired(this.isExpired());
        o.setId(this.id);
        o.setRedeemedAt(this.redeemedAt);
        o.setToken(this.token);
        o.setType(this.type);
        o.setValid(this.isValid());

        return o;
    }

}
