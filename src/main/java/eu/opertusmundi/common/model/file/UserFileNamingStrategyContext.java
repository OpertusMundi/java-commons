package eu.opertusmundi.common.model.file;

import lombok.Getter;

public class UserFileNamingStrategyContext extends FileNamingStrategyContext {

    protected UserFileNamingStrategyContext(String userName, boolean strict, boolean createIfNotExists) {
        super(createIfNotExists);

        this.userName = userName;
        this.strict   = strict;
    }

    @Getter
    private final boolean strict;

    @Getter
    private final String userName;

    public static UserFileNamingStrategyContext of(String userName) {
        return new UserFileNamingStrategyContext(userName, true, false);
    }

    public static UserFileNamingStrategyContext of(String userName, boolean strict) {
        return new UserFileNamingStrategyContext(userName, strict, false);
    }

    public static UserFileNamingStrategyContext of(String userName, boolean strict, boolean createIfNotExists) {
        return new UserFileNamingStrategyContext(userName, strict, createIfNotExists);
    }
}
