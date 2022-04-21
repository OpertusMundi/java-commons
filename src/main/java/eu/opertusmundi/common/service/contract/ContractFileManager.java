package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;
import java.util.UUID;

import eu.opertusmundi.common.model.file.FileSystemException;

public interface ContractFileManager {

    /**
     * Resolve contract path for a contract generated from a template
     *
     * @param userId the owner identifier of the order
     * @param orderKey the order key
     * @param itemIndex the order item index
     * @param signed {@code true} if the contract is signed
     *
     * @return
     * @throws FileSystemException
     */
    Path resolveMasterContractPath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed
    ) throws FileSystemException;

    /**
     * Resolve contract path for an uploaded contract
     *
     * @param userId the owner identifier of the order
     * @param orderKey the order key
     * @param itemIndex the order item index
     * @param signed {@code true} if the contract is signed
     *
     * @return
     * @throws FileSystemException
     */
    Path resolveUploadedContractPath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed
    ) throws FileSystemException;

}
