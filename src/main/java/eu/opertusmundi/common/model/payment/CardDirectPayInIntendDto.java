package eu.opertusmundi.common.model.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CardDirectPayInIntendDto {

    @Schema(description = "A custom description to appear on the user's bank statement")
    private String statementDescriptor;

}
