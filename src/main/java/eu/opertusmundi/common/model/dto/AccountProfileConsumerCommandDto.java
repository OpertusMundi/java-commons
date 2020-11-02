package eu.opertusmundi.common.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileConsumerCommandDto extends AccountProfileConsumerBaseDto {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

    @Schema(description = "Consumer billing address. Must reference an address from the user profile.")
    private UUID billingAddress;

    @Schema(description = "Consumer shipping address. Must reference an address from the user profile.")
    private UUID shippingAddress;

}
