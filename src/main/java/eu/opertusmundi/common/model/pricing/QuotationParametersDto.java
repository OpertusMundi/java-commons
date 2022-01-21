package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.opertusmundi.common.model.pricing.integration.SHImageQuotationParametersDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionQuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "User-defined quotation parameters")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", defaultImpl = EmptyQuotationParametersDto.class
)
@JsonSubTypes({
    @Type(name = "UNDEFINED", value = EmptyQuotationParametersDto.class),
    @Type(name = "FIXED_PER_ROWS", value = FixedRowQuotationParametersDto.class),
    @Type(name = "FIXED_FOR_POPULATION", value = FixedPopulationQuotationParametersDto.class),
    @Type(name = "PER_CALL_WITH_PREPAID", value = CallPrePaidQuotationParametersDto.class),
    @Type(name = "PER_ROW_WITH_PREPAID", value = RowPrePaidQuotationParametersDto.class),
    // External Data Provider pricing models
    @Type(name = "SENTINEL_HUB_SUBSCRIPTION", value = SHSubscriptionQuotationParametersDto.class),
    @Type(name = "SENTINEL_HUB_IMAGES", value = SHImageQuotationParametersDto.class),
})
@ToString
public class QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected QuotationParametersDto() {
        this.type = EnumPricingModel.UNDEFINED;
    }

    protected QuotationParametersDto(EnumPricingModel type) {
        this.type = type;
    }

    @Schema(
        description = "Discriminator field used for deserializing the model to the appropriate data type. "
                    + "The type of the quotation parameters must match the type of the pricing model to which "
                    + "they are applied.",
        example = "FIXED"
    )
    @JsonDeserialize(using = EnumPricingModel.Deserializer.class)
    @Getter
    @Setter
    private EnumPricingModel type;

}
