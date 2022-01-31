package eu.opertusmundi.common.util;

import com.github.slugify.Slugify;

public class TextUtils {

    public static String slugify(String value) {
        final Slugify s = new Slugify()
            .withTransliterator(true)
            .withLowerCase(true);

        return s.slugify(value);
    }
}
