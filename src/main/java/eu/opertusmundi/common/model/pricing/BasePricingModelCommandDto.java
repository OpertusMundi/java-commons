package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.opertusmundi.common.model.pricing.integration.SHImagePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.integration.SHSubscriptionPricingModelCommandDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "FREE", value = FreePricingModelCommandDto.class),
    @Type(name = "FIXED", value = FixedPricingModelCommandDto.class),
    @Type(name = "FIXED_PER_ROWS", value = FixedRowPricingModelCommandDto.class),
    @Type(name = "FIXED_FOR_POPULATION", value = FixedPopulationPricingModelCommandDto.class),
    @Type(name = "PER_CALL_WITH_PREPAID", value = CallPrePaidPricingModelCommandDto.class),
    @Type(name = "PER_CALL_WITH_BLOCK_RATE", value = CallBlockRatePricingModelCommandDto.class),
    @Type(name = "PER_ROW_WITH_PREPAID", value = RowPrePaidPricingModelCommandDto.class),
    @Type(name = "PER_ROW_WITH_BLOCK_RATE", value = RowBlockRatePricingModelCommandDto.class),
    // External Data Provider pricing models
    @Type(name = "SENTINEL_HUB_SUBSCRIPTION", value = SHSubscriptionPricingModelCommandDto.class),
    @Type(name = "SENTINEL_HUB_IMAGES", value = SHImagePricingModelCommandDto.class),
})
@Schema(
    description = "Pricing model command",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "FREE", schema = FreePricingModelCommandDto.class),
        @DiscriminatorMapping(value = "FIXED", schema = FixedPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "FIXED_PER_ROWS", schema = FixedRowPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "FIXED_FOR_POPULATION", schema = FixedPopulationPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "PER_CALL_WITH_PREPAID", schema = CallPrePaidPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "PER_CALL_WITH_BLOCK_RATE", schema = CallBlockRatePricingModelCommandDto.class),
        @DiscriminatorMapping(value = "PER_ROW_WITH_PREPAID", schema = RowPrePaidPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "PER_ROW_WITH_BLOCK_RATE", schema = RowBlockRatePricingModelCommandDto.class),
        // External Data Provider pricing models
        @DiscriminatorMapping(value = "SENTINEL_HUB_SUBSCRIPTION", schema = SHSubscriptionPricingModelCommandDto.class),
        @DiscriminatorMapping(value = "SENTINEL_HUB_IMAGES", schema = SHImagePricingModelCommandDto.class)
    }
)
public abstract class BasePricingModelCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected BasePricingModelCommandDto() {
        this.type = EnumPricingModel.UNDEFINED;
    }

    protected BasePricingModelCommandDto(EnumPricingModel type) {
        this.type = type;
    }

    @Schema(
        description = "Model unique identifier. This value is always generated at the server. "
                    + "Any value specified during an update request is ignored.",
        example = "a1c04890-9bb9-49f7-a880-6a75e3a561ad"
    )
    @Getter
    @Setter
    private String key;

    @Schema(
        description = "Discriminator field used for deserializing the model to the appropriate data type",
        example = "FIXED"
    )
    @JsonDeserialize(using = EnumPricingModel.Deserializer.class)
    @Getter
    @Setter
    private EnumPricingModel type;

    @ArraySchema(
        arraySchema = @Schema(
            description = "The domains in which users can apply the asset. Can be empty if no restrictions exist"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    private String[] domainRestrictions;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Continents that the users are allowed to apply the asset"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    private EnumContinent[] coverageRestrictionContinents;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Restrict consumers to specific continents"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    private EnumContinent[] consumerRestrictionContinents;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Countries that the users are allowed to apply the asset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(
            description = "The country 2 letter code as defined in ISO 3166",
            externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes")
        )
    )
    @Getter
    @Setter
    private String[] coverageRestrictionCountries;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Restrict consumers to specific countries"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(
            description = "The country 2 letter code as defined in ISO 3166",
            externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes")
        )
    )
    @Getter
    @Setter
    private String[] consumerRestrictionCountries;

    /**
     * Check if user parameters type is compatible with the pricing model
     *
     * @param params
     * @throws QuotationException
     */
    protected abstract void checkUserParametersType(QuotationParametersDto params) throws QuotationException;

    /**
     * Validate model
     *
     * @throws QuotationException
     */
    public abstract void validate() throws QuotationException;

    /**
     * Validate quotation parameters
     *
     * @param params
     * @param ignoreMissing Do not validate missing parameters
     * @throws QuotationException
     */
    public abstract void validate(@Nullable QuotationParametersDto params, boolean ignoreMissing) throws QuotationException;

    /**
     * Computes the effective pricing model given a valid quotation parameters
     * object
     *
     * @param userParams Pricing model specific quotation parameters set by the user
     * @param systemParams Pricing model specific quotation parameters set by the system
     * @return
     * @throws QuotationException
     */
    public abstract EffectivePricingModelDto compute(
        @Nullable QuotationParametersDto userParams, SystemQuotationParametersDto systemParams
    ) throws QuotationException;

}
