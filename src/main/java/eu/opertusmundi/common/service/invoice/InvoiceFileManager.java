package eu.opertusmundi.common.service.invoice;

import java.nio.file.Path;

import eu.opertusmundi.common.model.file.FileSystemException;

public interface InvoiceFileManager {

    /**
     * Resolve invoice path
     *
     * @param userId the owner identifier of the order
     * @param payInReferenceNumber the payment reference number
     *
     * @return
     * @throws FileSystemException
     */
    Path resolvePath(
        Integer userId, String payInReferenceNumber
    ) throws FileSystemException;

}
