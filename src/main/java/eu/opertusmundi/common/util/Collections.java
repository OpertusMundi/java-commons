package eu.opertusmundi.common.util;

import java.util.Collection;

public class Collections {

    public static <E> boolean isEmpty(Collection<E> c) {
        return c == null || c.isEmpty();
    }
}
