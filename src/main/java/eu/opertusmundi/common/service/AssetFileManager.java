package eu.opertusmundi.common.service;

import java.nio.file.Path;
import java.util.List;

import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface AssetFileManager {

    List<FileDto> getAdditionalResources(String pid) throws FileSystemException, AssetRepositoryException;

    Path resolveResourcePath(String pid, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveAdditionalResourcePath(String pid, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveMetadataPropertyPath(String pid, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveContractPath(String pid) throws FileSystemException, AssetRepositoryException;

    Path resolveContractAnnexPath(String pid, String fileName) throws FileSystemException, AssetRepositoryException;

}
