package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Schema(description = "Quotation parameters bag with all user and system defined parameters")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class QuotationParametersDto {

    /**
     * Tax percent set automatically by the platform
     */
    @JsonIgnore
    private BigDecimal taxPercent;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "User-defined parameter of array of NUTS codes. The codes are used for computing asset coverage and population"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(
            description = "NUTS codes",
            externalDocs = @ExternalDocumentation(url = "https://ec.europa.eu/eurostat/web/regions-and-cities/overview")
        )
    )
    private List<String> nuts;
    
    @Schema(description = "Selected prepaid tier index if feature is supported. If a tier is selected and the pricing "
                        + "model does not support prepaid tiers, quotation service will return a validation error")
    private Integer prePaidTier;
    
    @Schema(description = "System parameters")
    @JsonInclude(Include.NON_NULL)
    private SystemParameters systemParams;

    @Schema(description = "Dynamic system-defined parameters set during the creation of a quotation such as the selected number of rows or population")
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class SystemParameters {
        
        @Schema(description = "System-defined parameter of the number of rows selected. If the pricing model does not support "
                            + "number of rows parameter, this property is not set")
        @JsonInclude(Include.NON_NULL)
        private Long rows;
        
        @Schema(description = "System-defined parameter of the size of selected population. If the pricing model does not "
                            + "support population size parameter, this property is not set")
        @JsonInclude(Include.NON_NULL)
        private Long population;
        
        @Schema(description = "System-defined parameter of the selected percent of total population. If the pricing model does not "
                            + "support population size parameter, this property is not set")
        @JsonInclude(Include.NON_NULL)
        private Integer populationPercent;

        public static SystemParameters fromRows(long rows) {
            final SystemParameters p = new SystemParameters();
            p.setRows(rows);
            return p;
        }

        public static SystemParameters fromPopulation(long population, int populationPercent) {
            final SystemParameters p = new SystemParameters();
            p.setPopulation(population);
            p.setPopulationPercent(populationPercent);
            return p;
        }
    }
    
}
