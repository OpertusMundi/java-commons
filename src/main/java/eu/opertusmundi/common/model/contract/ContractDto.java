package eu.opertusmundi.common.model.contract;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "MASTER_CONTRACT", value = TemplateContractDto.class),
    @Type(name = "UPLOADED_CONTRACT", value = CustomContractDto.class),
})
@Getter
@Schema(
    description = "Sentinel Hub custom properties",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "MASTER_CONTRACT", schema = TemplateContractDto.class),
        @DiscriminatorMapping(value = "UPLOADED_CONTRACT", schema = CustomContractDto.class)
    }
)
public class ContractDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Contract template type")
    protected EnumContractType type;

}
