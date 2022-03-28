package eu.opertusmundi.common.model.favorite;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ASSET", value = FavoriteAssetCommandDto.class),
    @Type(name = "PROVIDER", value = FavoriteProviderCommandDto.class),
})
@Schema(
    description = "Favorite command",
    discriminatorMapping = {
        @DiscriminatorMapping(value = "ASSET", schema = FavoriteAssetCommandDto.class),
        @DiscriminatorMapping(value = "PROVIDER", schema = FavoriteProviderCommandDto.class)
    }
)
public abstract class FavoriteCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected FavoriteCommandDto(EnumFavoriteType type) {
        this.type = type;
    }

    @JsonIgnore
    private Integer userId;

    @Schema(description = "Command type", required = true)
    @NotNull
    private EnumFavoriteType type;

}
