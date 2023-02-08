package eu.opertusmundi.common.util;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class NutsUtils {

    /**
     * Removes overlapping values from an array of NUTS codes
     *
     * @param codes
     * @return An array of unique non-overlapping NUTS codes
     */
    public static String[] removeOverlappingCodes(String[] codes) {
        if (ArrayUtils.isEmpty(codes)) {
            return new String[]{};
        }
        final var result = Arrays.stream(codes)
            .filter(n1 -> !Arrays.stream(codes).anyMatch(n2 -> n1.startsWith(n2) && !n1.equals(n2)))
            .distinct()
            .toArray(String[]::new);

        return result;
    }

}
