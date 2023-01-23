package eu.opertusmundi.common.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

    public static String appendPath(String basePath, String relativePath) throws URISyntaxException {
        final URI relativeUri = new URI(relativePath);
        if (relativeUri.isAbsolute()) {
            return relativePath;
        }

        basePath     = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

        return basePath + "/" + relativePath;
    }

}
