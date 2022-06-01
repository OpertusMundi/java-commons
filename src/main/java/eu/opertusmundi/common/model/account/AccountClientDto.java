package eu.opertusmundi.common.model.account;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountClientDto {

    @JsonIgnore
    private Integer id;

    private ZonedDateTime createdOn;

    private ZonedDateTime revokedOn;

    @Schema(description = "User-defined client name")
    private String alias;

    @Schema(description = "Client id")
    @JsonProperty("clientId")
    private UUID clientId;

    /**
     * Client secret available only the first time the client is created
     */
    @Schema(description = "Client secret. Available only the first time a client is created")
    @JsonInclude(Include.NON_EMPTY)
    private String secret;

}
