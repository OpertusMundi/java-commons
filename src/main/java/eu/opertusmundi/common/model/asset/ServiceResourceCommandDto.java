package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServiceResourceCommandDto extends ResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Parent resource unique identifier")
    @JsonInclude(Include.NON_NULL)
    @Getter
    @Setter
    protected UUID parentId;

    @Schema(description = "Service type")
    private EnumSpatialDataServiceType serviceType;

    @Schema(description = "Service endpoint")
    private String endpoint;

    public static ServiceResourceCommandDto of(
        UUID publisherKey, UUID draftKey, UUID parentId, EnumSpatialDataServiceType serviceType, String endpoint
    ) {
        final ServiceResourceCommandDto c = new ServiceResourceCommandDto();
        c.setDraftKey(draftKey);
        c.setParentId(parentId);
        c.setPublisherKey(publisherKey);
        c.setEndpoint(endpoint);
        c.setServiceType(serviceType);
        return c;
    }

}
