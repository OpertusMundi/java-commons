package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.nio.file.Path;

public interface ContractFileNamingStrategy {

    /**
     * Resolves contract absolute file path
     *
     * @param ctx
     * @return
     * @throws IOException
     */
    Path resolvePath(ContractFileNamingStrategyContext ctx) throws IOException;

}
