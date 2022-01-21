package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
    @Getter
    private Long rows;

    @Schema(description = "System-defined parameter of the size of selected population. If the pricing model does not "
                        + "support population size parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @Getter
    private Long population;

    @Schema(description = "System-defined parameter of the selected percent of total population. If the pricing model does not "
                        + "support population size parameter, this property is not set")
    @JsonInclude(Include.NON_NULL)
    @Getter
    private Integer populationPercent;

    public static SystemQuotationParametersDto of(BigDecimal taxPercent) {
        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent = taxPercent;
        return p;
    }

    public static SystemQuotationParametersDto ofRows(BigDecimal taxPercent, long rows) {
        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent = taxPercent;
        p.rows       = rows;
        return p;
    }

    public static SystemQuotationParametersDto ofPopulation(BigDecimal taxPercent, long population, int populationPercent) {

        final SystemQuotationParametersDto p = new SystemQuotationParametersDto();
        p.taxPercent        = taxPercent;
        p.population        = population;
        p.populationPercent = populationPercent;
        return p;
    }

}
