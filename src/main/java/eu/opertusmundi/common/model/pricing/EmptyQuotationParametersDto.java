package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

public class EmptyQuotationParametersDto extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public EmptyQuotationParametersDto() {
        super(EnumPricingModel.UNDEFINED);
    }

}
