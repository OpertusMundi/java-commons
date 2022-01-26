package eu.opertusmundi.common.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.mangopay.core.enumerations.CountryIso;

import eu.opertusmundi.common.model.EnumReferenceType;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;

public final class MangopayUtils {

    public static CountryIso countryFromString(String value) {
        for (final CountryIso v : CountryIso.values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid country code [%s]", value));
    }

    /**
     * Create a unique reference number per {@link EnumReferenceType} value
     *
     * @see https://docs.mangopay.com/endpoints/v2.01/payins#e264_the-payin-object
     * @see https://docs.mangopay.com/guide/customising-bank-statement-references
     *
     * @param type
     * @param value
     * @return
     */
    public static String createReferenceNumber(EnumReferenceType type, int value) {
        final String digits     = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
        final int    targetBase = digits.length();
        String       result     = "";

        do {
            result = digits.charAt(value % targetBase) + result;
            value  = value / targetBase;
        } while (value > 0);

        final int    padLength = 10 - type.getPrefix().length();
        final String reference = type.getPrefix() + StringUtils.leftPad(result, padLength, "0");

        Assert.isTrue(reference.length() == 10, "Reference number length is out of bounds!");

        return reference;
    }

    public static String createStatementDescriptor(CardDirectPayInCommand command) {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getReferenceNumber(), "Expected a non-null reference number");

        // Use reference number as the statement descriptor
        final String result = command.getReferenceNumber();

        Assert.isTrue(result.length() < 11, "Statement descriptor can be up to 10 characters long");

        return result;
    }

}
