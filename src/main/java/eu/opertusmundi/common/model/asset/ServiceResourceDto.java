package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.UUID;

import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ServiceResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Service type")
    @Getter
    @Setter
    private EnumServiceResourceType serviceType;

    @Schema(description = "Service endpoint")
    @Getter
    @Setter
    private String endpoint;

    @JsonCreator
    public ServiceResourceDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("parentId") UUID parentId,
        @JsonProperty("serviceType") EnumServiceResourceType serviceType,
        @JsonProperty("endpoint") String endpoint
    ) {
        this.id          = id;
        this.parentId    = parentId;
        this.type        = EnumResourceType.SERVICE;
        this.serviceType = serviceType;
        this.endpoint    = endpoint;
    }

    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == this.type);

        final ServiceResourceDto resource = (ServiceResourceDto) r;
        // Id and type are immutable
        this.serviceType = resource.serviceType;
        this.endpoint    = resource.endpoint;
    }

}
