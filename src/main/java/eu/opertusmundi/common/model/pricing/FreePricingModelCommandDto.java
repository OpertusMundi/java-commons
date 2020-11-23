package eu.opertusmundi.common.model.pricing;

public class FreePricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FreePricingModelCommandDto() {
        super(EnumPricingModel.FREE);
    }

}