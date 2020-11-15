package eu.opertusmundi.common.model.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import eu.opertusmundi.common.model.dto.EnumCustomerType;

@Converter(autoApply = false)
public class EnumCustomerTypeAttributeConverter implements AttributeConverter<EnumCustomerType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EnumCustomerType attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute.getValue();
    }

    @Override
    public EnumCustomerType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        return EnumCustomerType.fromValue(dbData);
    }

}
