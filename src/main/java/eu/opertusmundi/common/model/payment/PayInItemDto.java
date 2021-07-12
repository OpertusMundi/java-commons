package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class PayInItemDto {

    @JsonIgnore
    protected Integer id;

    @Schema(description = "Invoice line number")
    protected Integer index;

    @Schema(description = "Parent PayIn unique key")
    protected UUID payIn;

    @Schema(description = "Payment item type")
    protected EnumPaymentItemType type;

}
