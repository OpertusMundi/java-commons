package eu.opertusmundi.common.model.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class UserBlockedStatusDto {

    @Schema(description = "Consumer block status or `null` if user is not a registered consumer")
    final protected BlockStatusDto consumer;

    @Schema(description = "Provider block status or `null` if user is not a registered provider")
    final protected BlockStatusDto provider;

}
