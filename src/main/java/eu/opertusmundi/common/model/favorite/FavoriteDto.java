package eu.opertusmundi.common.model.favorite;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ASSET", value = FavoriteAssetDto.class),
    @Type(name = "PROVIDER", value = FavoriteProviderDto.class),
})
@Schema(
    description = "Favorite",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "ASSET", schema = FavoriteAssetDto.class),
        @DiscriminatorMapping(value = "PROVIDER", schema = FavoriteProviderDto.class)
    }
)
public class FavoriteDto {

    @JsonIgnore
    protected Integer id;

    @Schema(description = "Favorite unique identifier")
    protected UUID key;

    @Schema(description = "Favorite type")
    protected EnumFavoriteType type;

    @JsonIgnore
    protected String title;

}
