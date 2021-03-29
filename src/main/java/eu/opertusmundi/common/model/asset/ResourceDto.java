package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
    @Type(name = "FILE", value = FileResourceDto.class),
    @Type(name = "SERVICE", value = ServiceResourceDto.class),
})
public abstract class ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Resource unique identifier")
    @Getter
    @Setter
    protected UUID id;

    @Schema(description = "Parent resource unique identifier")
    @JsonInclude(Include.NON_NULL)
    @Getter
    @Setter
    protected UUID parentId;

    @Schema(description = "Discriminator field used for deserializing the model to the appropriate data type", example = "FILE")
    @Getter
    @Setter
    protected EnumResourceType type;
    
    public abstract void patch(ResourceDto r);

}
