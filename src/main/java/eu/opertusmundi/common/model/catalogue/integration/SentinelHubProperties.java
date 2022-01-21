package eu.opertusmundi.common.model.catalogue.integration;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

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
    @Type(name = "OPEN_DATA", value = OpenDataSentinelHubProperties.class),
    @Type(name = "COMMERCIAL", value = CommercialDataSentinelHubProperties.class),
})
@Getter
@Setter
public class SentinelHubProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EnumType {
        OPEN_DATA,
        COMMERCIAL,
        ;
    }

    protected SentinelHubProperties(EnumType type) {
        this.type = type;
    }

    @Schema(description = "Dataset type", required = true)
    @NotNull
    private EnumType type;

}
