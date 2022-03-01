package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.AccountRecentSearchDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountRecentSearch")
@Table(schema = "web", name = "`account_recent_search`")
public class AccountRecentSearchEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_recent_search_id_seq", name = "account_recent_search_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_recent_search_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @Column(name = "`value`")
    @Getter
    @Setter
    private String value;

    @NotNull
    @Column(name = "`added_on`")
    @Getter
    private final ZonedDateTime addedOn = ZonedDateTime.now();

    public AccountRecentSearchDto toDto() {
        return AccountRecentSearchDto.of(value, addedOn);
    }

}
