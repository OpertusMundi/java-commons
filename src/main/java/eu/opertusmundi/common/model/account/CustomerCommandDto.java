package eu.opertusmundi.common.model.account;

import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CustomerCommandDto {

    protected CustomerCommandDto() {
        this.type = EnumMangopayUserType.UNDEFINED;
    }

    protected CustomerCommandDto(EnumMangopayUserType type) {
        this.type = type;
    }

    @JsonIgnore
    protected UUID contract;

    @JsonIgnore
    @Setter(AccessLevel.PRIVATE)
    protected EnumCustomerType customerType;

    @JsonIgnore
    protected String paymentProviderUser;

    @JsonIgnore
    protected String paymentProviderWallet;

    @JsonIgnore
    protected Integer userId;

    @Schema(description = "Customer email address")
    @NotEmpty
    @Email
    @Size(min = 1, max = 255)
    protected String email;

    @Schema(description = "MANGOPAY user type")
    @NotNull
    protected EnumMangopayUserType type;

}
