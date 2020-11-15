package eu.opertusmundi.common.model.dto;

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
    @Type(name = "INDIVIDUAL", value = ConsumerIndividualCommandDto.class),
    @Type(name = "PROFESSIONAL", value = ProviderProfessionalCommandDto.class),
})
@Getter
@Setter
public abstract class CustomerCommandDto {

    protected CustomerCommandDto() {
        this.type = EnumCustomerType.UNDEFINED;
    }

    protected CustomerCommandDto(EnumCustomerType type) {
        this.type = type;
    }

    @JsonIgnore
    protected UUID contract;

    @Schema(description = "Customer email address")
    @NotEmpty
    protected String email;

    @JsonIgnore
    protected Integer userId;

    @JsonIgnore
    protected String paymentProviderUser;

    @JsonIgnore
    protected String paymentProviderWallet;

    @Schema(description = "Customer type")
    @NotNull
    protected EnumCustomerType type;

}
