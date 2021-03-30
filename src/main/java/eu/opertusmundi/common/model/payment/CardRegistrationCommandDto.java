package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Command for completing the registration of a card
 * 
 * The owner identifier is derived from
 * {@link CardRegistrationCommandDto#userKey} member.
 */
@Schema(description = "Card registration data required by the API Gateway for completing the registration of card")
@NoArgsConstructor
@Getter
@Setter
public class CardRegistrationCommandDto {

    @JsonIgnore
    private UUID userKey;

    @Schema(description = "Registration identifier created when initialized the card registration")
    @NotEmpty
    private String registrationId;
    
    @Schema(
        description = "Card registration data returned by the tokenization server.",
        externalDocs = @ExternalDocumentation(url = "https://docs.mangopay.com/endpoints/v2.01/cards#e1042_post-card-info"),
        example = "data=gcpSOxwNHZutpFWmFCAYQu1kk25qPfJFdPaHT9kM3gKumDF3GeqSw8f-k8nh-s5OC3GNnhGoF"
    )
    @NotEmpty
    private String registrationData;
    
}
