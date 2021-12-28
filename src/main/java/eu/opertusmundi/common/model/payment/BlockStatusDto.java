package eu.opertusmundi.common.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class BlockStatusDto {

    @JsonIgnore
    @Schema(
        description = "A code corresponding to a specific action to unblock the user",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/guide/blocked-users")
    )
    final String actionCode;

    @Schema(description = "`true` if a user has her inflows (payins, incoming transfers) blocked")
    final Boolean inflows;

    @Schema(description = "`true` if a user has her outflows (payouts, outgoing tranfers) blocked")
    final Boolean outflows;

}
