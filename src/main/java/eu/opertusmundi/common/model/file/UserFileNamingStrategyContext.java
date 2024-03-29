package eu.opertusmundi.common.model.file;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import lombok.Getter;

public class UserFileNamingStrategyContext extends FileNamingStrategyContext {

    private static final Predicate<String> strictNameMatchPredicate =
        Pattern.compile("^[-_a-z0-9]+([.][-_a-z0-9]+)*$", Pattern.CASE_INSENSITIVE).asPredicate();

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

    @Override
    public boolean validateName(int level, String name) {
        Assert.hasText(name, "A path component must be non empty");

        if (level == 0 && name.equals(EnumUserFileReservedEntry.NOTEBOOKS_FOLDER.entryName())) {
            return true;
        }
        return !strict || strictNameMatchPredicate.test(name);
    }
}
