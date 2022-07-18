package eu.opertusmundi.common.service.invoice;

import java.io.IOException;
import java.util.UUID;

public interface InvoiceGeneratorService {

    /**
     * Create invoice PDF
     *
     * @param command
     * @return
     * @throws IOException
     */
    String generateInvoicePdf(UUID payinKey) throws IOException;

}
