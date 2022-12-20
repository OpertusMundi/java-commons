package eu.opertusmundi.common.model.catalogue;

import lombok.Getter;

@Getter
public class IdVersionPair {

    private IdVersionPair(String id, String version) {
        super();
        this.id      = id;
        this.version = version;
    }

    public static IdVersionPair of(String id) {
        return IdVersionPair.of(id, "");
    }

    public static IdVersionPair of(String id, String version) {
        return new IdVersionPair(id, version);
    }

    private final String id;
    private final String version;

}