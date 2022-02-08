package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.file.DirectoryDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileUploadCommand;
import eu.opertusmundi.common.model.file.QuotaDto;

public interface UserFileManager {

    @Nullable
    QuotaDto getQuota(String userName) throws FileSystemException;

    DirectoryDto browse(FilePathCommand command) throws FileSystemException;

    void createPath(FilePathCommand command) throws FileSystemException;

    void uploadFile(InputStream input, FileUploadCommand command) throws FileSystemException;

    void deletePath(FilePathCommand command) throws FileSystemException;

    Path resolveFilePath(FilePathCommand command) throws FileSystemException;

}
