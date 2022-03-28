package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ASSET", value = BundleAssetResourceDto.class),
    @Type(name = "FILE", value = FileResourceDto.class),
    @Type(name = "SERVICE", value = ServiceResourceDto.class),
})
@Schema(
    description = "Resource",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "ASSET", schema = BundleAssetResourceDto.class),
        @DiscriminatorMapping(value = "FILE", schema = FileResourceDto.class),
        @DiscriminatorMapping(value = "SERVICE", schema = ServiceResourceDto.class)
    }
)
public abstract class ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public ResourceDto(EnumResourceType type) {
        super();
        this.type     = type;
    }

    public ResourceDto(String id, String parentId, EnumResourceType type) {
        super();

        this.id       = id;
        this.parentId = parentId;
        this.type     = type;
    }

    @Schema(description = "Resource unique identifier")
    protected String id;

    @Schema(description = "Resource parent unique identifier")
    @JsonInclude(Include.NON_EMPTY)
    protected String parentId;

    @Schema(description = "Discriminator field used for deserializing the model to the appropriate data type", example = "FILE")
    protected EnumResourceType type;

    public abstract void patch(ResourceDto r);

    public abstract CatalogueResource toCatalogueResource();

    public static ResourceDto fromCatalogueResource(CatalogueResource r) {
        switch (r.getType()) {
            case FILE :
                return new FileResourceDto(r);
            case SERVICE :
                return new ServiceResourceDto(r);
            default:
                throw ApplicationException.fromMessage(
                    BasicMessageCode.NotImplemented,
                    String.format("Resource type [%s] is not supported", r.getType())
                );
        }
    }

}
