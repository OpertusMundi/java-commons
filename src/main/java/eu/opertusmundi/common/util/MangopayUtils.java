package eu.opertusmundi.common.util;

import com.mangopay.core.enumerations.CountryIso;

public final class MangopayUtils {

    public static CountryIso countryFromString(String value) {
        for (final CountryIso v : CountryIso.values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid country code [%s]", value));
    }

}
