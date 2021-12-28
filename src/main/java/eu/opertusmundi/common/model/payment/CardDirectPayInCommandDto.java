package eu.opertusmundi.common.model.payment;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CardDirectPayInCommandDto {

    @Schema(
        description = "Contains every useful information related to the user billing",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/guide/3ds2-integration")
    )
    private PayInAddressCommandDto billing;

    @NotNull
    @Valid
    @Schema(
        description = "This object describes the Browser being user by the end user",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/guide/3ds2-integration")
    )
    private BrowserInfoDto browserInfo;

    @Schema(description = "A registered card unique identifier")
    private String cardId;

    @Schema(
        description = "Contains every useful information related to the user shipping",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/guide/3ds2-integration")
    )
    private PayInAddressCommandDto shipping;

}
