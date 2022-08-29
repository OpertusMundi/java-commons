package eu.opertusmundi.common.util;

import com.github.slugify.Slugify;

public class TextUtils {

    public static String slugify(String value) {
        final Slugify s = Slugify.builder()
            .transliterator(true)
            .lowerCase(true)
            .build();

        return s.slugify(value);
    }
}
