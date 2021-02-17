package eu.opertusmundi.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetFileNamingStrategyContext extends FileNamingStrategyContext {

    protected AssetFileNamingStrategyContext(String pid) {
        super(false);

        this.pid = pid;
    }

    private String pid;

    public static AssetFileNamingStrategyContext of(String pid) {
        return new AssetFileNamingStrategyContext(pid);
    }

}
