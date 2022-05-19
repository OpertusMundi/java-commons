package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.rating.RatingDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetRating")
@Table(name = "`asset`", schema = "rating")
public class AssetRatingEntity extends RatingEntity {

    @NotNull
    @Column(name = "`asset`", updatable = false)
    @Getter
    @Setter
    private String asset;

    @Column(name = "`account`", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID account;

    @Column(name = "`value`", columnDefinition = "numeric", precision = 2, scale = 1)
    @Getter
    @Setter
    private BigDecimal value;

    @Column(name = "`comment`")
    @Getter
    @Setter
    private String comment;

    @Column(name = "`created_on`")
    @Getter
    ZonedDateTime createdAt = ZonedDateTime.now();

    public AssetRatingEntity() {

    }

    public RatingDto toDto() {
        final RatingDto r = new RatingDto();

        r.setComment(this.comment);
        r.setCreatedAt(this.createdAt);
        r.setValue(this.value);

        return r;
    }

}