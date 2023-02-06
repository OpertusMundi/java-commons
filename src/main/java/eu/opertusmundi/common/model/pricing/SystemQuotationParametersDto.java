package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "Dynamic system-defined parameters set during the creation of a quotation such as the selected number of rows or population")
@NoArgsConstructor
@ToString
public class SystemQuotationParametersDto implements Serializable {

    @Schema(description = "Tax applied based on the billing region")
    @Getter
    protected BigDecimal taxPercent;

    private static final long serialVersionUID = 1L;

    @Schema(description = "System-defined parameter of the number of rows selected. If the pricing model does not support "
                        + "number of rows parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @JsonProperty("rows")
    @Getter
    private Long selectedRows;

    @Schema(description = "System-defined parameter of the total number of rows. If the pricing model does not support "
                        + "number of rows parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @Getter
    private Long totalRows;

    @Schema(description = "System-defined parameter of the size of selected population. If the pricing model does not "
                        + "support population size parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @JsonProperty("population")
    @Getter
    private Long selectedPopulation;

    @Schema(description = "System-defined parameter of the total population. If the pricing model does not "
                        + "support population size parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @Getter
    private Long totalPopulation;

    @Schema(description = "System-defined parameter of the selected percent of total population. If the pricing model does not "
                        + "support population size parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @JsonProperty("populationPercent")
    @Getter
    private Integer selectedPopulationPercent;

    public static SystemQuotationParametersDto of(BigDecimal taxPercent) {
        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent = taxPercent;
        return p;
    }

    public static SystemQuotationParametersDto ofRows(BigDecimal taxPercent, long selectedRows, long totalRows) {
        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent   = taxPercent;
        p.selectedRows = selectedRows;
        p.totalRows    = totalRows;
        return p;
    }

    public static SystemQuotationParametersDto ofPopulation(
        BigDecimal taxPercent, long selectedPopulation, int selectedPopulationPercent, long totalPopulation
    ) {

        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent                = taxPercent;
        p.selectedPopulation        = selectedPopulation;
        p.selectedPopulationPercent = selectedPopulationPercent;
        p.totalPopulation           = totalPopulation;
        return p;
    }

}
