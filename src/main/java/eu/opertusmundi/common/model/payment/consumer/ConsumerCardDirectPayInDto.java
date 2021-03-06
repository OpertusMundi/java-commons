package eu.opertusmundi.common.model.payment.consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A card direct PayIn
 */
@NoArgsConstructor
@Getter
@Setter
public class ConsumerCardDirectPayInDto extends ConsumerPayInDto {

    @JsonIgnore
    private String card;

    @Schema(description = "A partially obfuscated version of the credit card number")
    @JsonInclude(Include.NON_EMPTY)
    private String alias;

    @Schema(
        description = "A custom description to appear on the user's bank statement",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/guide/customising-bank-statement-references-direct-debit")
    )
    private String statementDescriptor;

    @Schema(description = "Redirect URL if 3-D Secure validation is required")
    @JsonInclude(Include.NON_NULL)
    private String secureModeRedirectURL;

}
