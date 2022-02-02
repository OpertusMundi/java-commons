package eu.opertusmundi.common.model.file;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
    public boolean validateName(String name) {
        Assert.state(!StringUtils.isEmpty(name), "A path component must be non empty");
        return !strict || strictNameMatchPredicate.test(name); 
    }
}
