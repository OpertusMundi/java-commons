package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A generic {@link MessageCode} that simply holds a key.
 * <p>
 * This is basically useful as a deserializer of an {@link MessageCode} of an
 * unknown concrete class.
 */
public class GenericMessageCode implements MessageCode {
    private final String key;

    @JsonCreator
    public GenericMessageCode(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return this.key;
    }

}