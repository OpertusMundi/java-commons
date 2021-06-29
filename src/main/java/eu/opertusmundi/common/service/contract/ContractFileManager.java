package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface ContractFileManager {

    Path resolvePath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed, boolean present
    ) throws FileSystemException, AssetRepositoryException;

}
