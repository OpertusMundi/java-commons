package eu.opertusmundi.common.model.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CardDirectPayInCommandDto {

    @Schema(description = "A registered card unique identifier")
    private String cardId;

}
