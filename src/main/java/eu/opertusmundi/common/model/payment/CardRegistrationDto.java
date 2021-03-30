package eu.opertusmundi.common.model.payment;

import javax.validation.constraints.NotEmpty;

import com.mangopay.entities.CardRegistration;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A card registration
 */
@Schema(description = "Card registration data required by the tokenization server")
@NoArgsConstructor
@Getter
@Setter
public class CardRegistrationDto {

    protected CardRegistrationDto(CardRegistration r) {
        this.registrationId      = r.getId();
        this.accessKey           = r.getAccessKey();
        this.cardRegistrationUrl = r.getCardRegistrationUrl();
        this.preRegistrationData = r.getPreregistrationData();
    }

    public static CardRegistrationDto from(CardRegistration r) {
        return new CardRegistrationDto(r);
    }

    @Schema(description = "Registration identifier required for submitting the tokenization server response")
    @NotEmpty
    private String registrationId;

    @Schema(
        description = "Value required for property `accessKeyRef` of tokenization server request",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/endpoints/v2.01/cards#e1042_post-card-info")
    )
    private String accessKey;
    
   
    @Schema(
        description = "Card tokenization server URL. Client must submit the card details to this URL",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/endpoints/v2.01/cards#e1042_post-card-info")
    )
    private String cardRegistrationUrl;
    
    @Schema(
        description = "Value required for property `data` of tokenization server request",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/endpoints/v2.01/cards#e1042_post-card-info")
    )
    private String preRegistrationData;

}
