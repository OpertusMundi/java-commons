package eu.opertusmundi.common.validation;

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.mangopay.core.enumerations.CountryIso;

public class IsoCountryCodeValidator implements ConstraintValidator<IsoCountryCode, String> {

    @Override
    public void initialize(IsoCountryCode constraint) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return Arrays.stream(CountryIso.values()).anyMatch(v -> v.name().equalsIgnoreCase(value));
    }

}