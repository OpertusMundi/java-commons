package eu.opertusmundi.common.model.catalogue.client;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ASSET", value = DraftApiFromAssetCommandDto.class),
    @Type(name = "FILE", value = DraftApiFromFileCommandDto.class),
})
@Getter
@Setter
public abstract class DraftApiCommandDto {

    protected DraftApiCommandDto(EnumDraftCommandType type) {
        this.type = type;
    }

    @JsonIgnore
    private Integer userId;

    @JsonIgnore
    private UUID publisherKey;

    @Schema(description = "Command type", required = true)
    @NotNull
    private EnumDraftCommandType type;

    @Schema(description = "Service type", allowableValues = {"WMS", "WFS", "DATA_API"}, required = true)
    @NotNull
    private String serviceType;
    
    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "The version of the resource", required = true)
    @NotEmpty
    private String version;

}
