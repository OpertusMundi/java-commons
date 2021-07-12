package eu.opertusmundi.common.model.account;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class ConsumerDto {

    @Schema(description = "Consumer unique key")
    @JsonProperty("id")
    private UUID key;

    @Schema(description = "Consumer name")
    private String name;

    @Schema(description = "Consumer country")
    private String country;

}