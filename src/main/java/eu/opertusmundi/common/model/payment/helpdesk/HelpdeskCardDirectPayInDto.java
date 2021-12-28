package eu.opertusmundi.common.model.payment.helpdesk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.payment.BrowserInfoDto;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentType;
import eu.opertusmundi.common.model.payment.PayInAddressDto;
import eu.opertusmundi.common.model.payment.PayInRecurringRegistrationDto;
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
public class HelpdeskCardDirectPayInDto extends HelpdeskPayInDto {

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

    @Schema(description = "Information related to the user billing address")
    private PayInAddressDto billing;

    @Schema(description = "Information related to the user shipping address")
    private PayInAddressDto shipping;

    @Schema(description = "Browser information required by 3DS2 integration")
    private BrowserInfoDto browserInfo;

    @Schema(description = "Recurring payment information")
    private PayInRecurringRegistrationDto recurringPayment;

    @Schema(description = "Recurring payment type")
    private EnumRecurringPaymentType recurringPaymentType;

}
