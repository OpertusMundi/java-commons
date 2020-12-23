package eu.opertusmundi.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFileNamingStrategyContext extends FileNamingStrategyContext {

    protected UserFileNamingStrategyContext(int id, boolean createIfNotExists) {
        super(createIfNotExists);

        this.id = id;
    }

    private int id;

    public static UserFileNamingStrategyContext of(int id) {
        return new UserFileNamingStrategyContext(id, false);
    }

    public static UserFileNamingStrategyContext of(int id, boolean createIfNotExists) {
        return new UserFileNamingStrategyContext(id, createIfNotExists);
    }
}
