package eu.opertusmundi.common.service.invoice;

import java.io.IOException;
import java.nio.file.Path;

public interface InvoiceFileNamingStrategy {

    /**
     * Resolves invoice absolute file path
     *
     * @param ctx
     * @return
     * @throws IOException
     */
    Path resolvePath(InvoiceFileNamingStrategyContext ctx) throws IOException;

}
