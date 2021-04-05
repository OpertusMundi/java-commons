package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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
})
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
    protected UUID key;

    @Schema(
        description = "Discriminator field used for deserializing the model to the appropriate data type",
        example = "FIXED"
    )
    @JsonDeserialize(using = EnumPricingModel.Deserializer.class)
    @Getter
    @Setter
    protected EnumPricingModel type;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "The domains in which users can apply the asset. Can be empty if no restrictions exist"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    protected String[] domainRestrictions;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "Continents that the users are allowed to apply the asset"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    protected EnumContinent[] coverageRestrictionContinents;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "Restrict consumers to specific continents"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @Getter
    @Setter
    protected EnumContinent[] consumerRestrictionContinents;
    
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
    protected String[] coverageRestrictionCountries;
    
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
    protected String[] consumerRestrictionCountries;
    
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
     * @throws QuotationException
     */
    public abstract void validate(QuotationParametersDto params) throws QuotationException;
    
    /**
     * Computes the effective pricing model given a valid quotation parameters
     * object
     * 
     * 
     * @param params
     * @return
     */
    public abstract EffectivePricingModelDto compute(QuotationParametersDto params);
    
}