package eu.opertusmundi.common.model.file;

import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.common.model.ServiceException;

public class FileSystemException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public FileSystemException(FileSystemMessageCode code) {
        super(code, "An I/O error has occurred");
    }

    public FileSystemException(String message) {
        super(FileSystemMessageCode.IO_ERROR, message);
    }

    public FileSystemException(FileSystemMessageCode code, String message) {
        super(code, message);
    }

    public FileSystemException(FileSystemMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}