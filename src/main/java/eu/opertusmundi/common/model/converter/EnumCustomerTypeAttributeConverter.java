package eu.opertusmundi.common.model.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import eu.opertusmundi.common.model.account.EnumMangopayUserType;

@Converter(autoApply = false)
public class EnumCustomerTypeAttributeConverter implements AttributeConverter<EnumMangopayUserType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EnumMangopayUserType attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.getValue();
    }

    @Override
    public EnumMangopayUserType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        return EnumMangopayUserType.fromValue(dbData);
    }

}
