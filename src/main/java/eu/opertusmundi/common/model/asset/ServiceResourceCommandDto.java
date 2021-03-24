package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServiceResourceCommandDto extends BaseResourceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Service type")
    private EnumServiceResourceType serviceType;

    @Schema(description = "Service endpoint")
    private String endpoint;

    public static ServiceResourceCommandDto of(UUID publisherKey, UUID draftKey, EnumServiceResourceType serviceType, String endpoint) {
        final ServiceResourceCommandDto c = new ServiceResourceCommandDto();
        c.setDraftKey(draftKey);
        c.setPublisherKey(publisherKey);
        c.setEndpoint(endpoint);
        c.setServiceType(serviceType);
        return c;
    }
    public ServiceResourceDto toResource() {
        return new ServiceResourceDto(UUID.randomUUID(), serviceType, endpoint);
    }

}
