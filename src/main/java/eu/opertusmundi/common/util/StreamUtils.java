package eu.opertusmundi.common.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class StreamUtils {

    /**
     * Creates a stream from a collection. If the collection is {@code null}, an
     * empty stream is returned
     * 
     * @param <T>
     * @param collection
     * @return
     */
    public static <T> Stream<T> from(Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }
    
}
