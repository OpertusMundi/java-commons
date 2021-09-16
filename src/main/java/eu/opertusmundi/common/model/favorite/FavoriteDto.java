package eu.opertusmundi.common.model.favorite;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
public abstract class FavoriteDto {

    @JsonIgnore
    protected Integer id;

    @Schema(description = "Favorite unique identifier")
    protected UUID key;

    @Schema(description = "Favorite type")
    protected EnumFavoriteType type;

    @Schema(description = "Favorite short description")
    protected String title;

}
