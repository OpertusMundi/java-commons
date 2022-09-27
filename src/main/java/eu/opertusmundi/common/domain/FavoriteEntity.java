package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Favorite")
@Table(schema = "web", name = "`favorite`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_favorite_key", columnNames = {"`key`"}),
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "`type`", discriminatorType = DiscriminatorType.STRING)
public abstract class FavoriteEntity {

    protected FavoriteEntity() {
    }

    protected FavoriteEntity(EnumFavoriteType type) {
        this.type = type;
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.favorite_id_seq", name = "favorite_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "favorite_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    protected Integer id;

    @NotNull
    @NaturalId
    @Column(name = "`key`", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    protected UUID key;

    /**
     * Reference to the owning account
     */
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    protected AccountEntity account;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    protected ZonedDateTime createdOn;

    @NotEmpty
    @Column(name = "`title`")
    @Getter
    @Setter
    protected String title;

    @NotNull
    @Column(name = "`type`", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    protected EnumFavoriteType type;

    public abstract FavoriteDto toDto(boolean includeDetails);

}
