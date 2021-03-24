package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.Validity;
import com.mangopay.entities.Card;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A registered card
 */
@NoArgsConstructor
@Getter
@Setter
public class CardDto {

    protected CardDto(Card c) {
        this.active         = c.isActive();
        this.alias          = c.getAlias();
        this.cardType       = EnumCardType.fromCardType(c.getCardType());
        this.currency       = c.getCurrency().toString();
        this.expirationDate = c.getExpirationDate();
        this.id             = c.getId();
        this.validity       = c.getValidity();
    }

    @Schema(description = "Whether the card is active or not")
    private boolean active;

    @Schema(description = "A partially obfuscated version of the credit card number")
    private String alias;

    @Schema(description = "The type of card. Currenlty only a single card type is supported, `CB_VISA_MASTERCARD`.")
    private EnumCardType cardType;

    @Schema(
        description = "The currency in ISO 4217 format", 
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_4217")
    )
    private String currency;

    @Schema(description = "The expiry date of the card in `MMYY` format")
    private String expirationDate;

    @Schema(description = "Card unique identifier. This identifier is required for creating a card direct PayIn")
    private String id;
    
    @Schema(description = "Card validity. A successful transaction (PreAuthorization or Payin) is required to validate a card id")
    private Validity validity;
    
    @Schema(description = "The expiry month of the card")
    public int expirationMonth() {
        return Integer.parseInt(this.expirationDate.substring(0, 2));
    }

    @Schema(description = "The expiry year of the card")
    public int expirationYear() {
        return Integer.parseInt(this.expirationDate.substring(2, 2));
    }

    public static CardDto from(Card c) {
        return new CardDto(c);
    }

}
