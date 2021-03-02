package eu.opertusmundi.common.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class StreamUtils {

    public static <T> Stream<T> from(Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }
    
}
