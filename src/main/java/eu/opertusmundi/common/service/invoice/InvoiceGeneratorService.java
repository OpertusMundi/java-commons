package eu.opertusmundi.common.service.invoice;

import java.io.IOException;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumInvoiceType;

public interface InvoiceGeneratorService {

    /**
     * Create invoice PDF for a PayIn record
     *
     * @param type
     * @param payinKey
     * @return
     * @throws IOException
     */
    String generateInvoicePdf(EnumInvoiceType type, UUID payinKey) throws IOException;

}
