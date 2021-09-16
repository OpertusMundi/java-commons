package eu.opertusmundi.common.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import eu.opertusmundi.common.model.favorite.FavoriteProviderDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "FavoriteProvider")
@Table(schema = "web", name = "`favorite_provider`")
@DiscriminatorValue(value = "PROVIDER")
public class FavoriteProviderEntity extends FavoriteEntity {

    public FavoriteProviderEntity() {
        super(EnumFavoriteType.PROVIDER);
    }

    /**
     * Reference to the provider account
     */
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "provider", nullable = false)
    @Getter
    @Setter
    protected AccountEntity provider;

    @Override
    public FavoriteDto toDto(boolean includeDetails) {
        final FavoriteProviderDto f = new FavoriteProviderDto();

        f.setId(id);
        f.setKey(key);
        f.setProvider(provider.getProvider().toProviderDto(includeDetails));
        f.setTitle(title);
        f.setType(type);

        return f;
    }


}
