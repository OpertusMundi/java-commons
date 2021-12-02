package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;
import java.util.UUID;

import eu.opertusmundi.common.model.file.FileSystemException;

public interface ContractFileManager {

    /**
     * Resolve contract path
     *
     * @param userId the owner identifier of the order
     * @param orderKey the order key
     * @param itemIndex the order item index
     * @param signed {@code true} if the contract is signed
     *
     * @return
     * @throws FileSystemException
     */
    Path resolvePath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed
    ) throws FileSystemException;

}
