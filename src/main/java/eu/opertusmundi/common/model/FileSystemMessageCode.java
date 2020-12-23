package eu.opertusmundi.common.model;

public enum FileSystemMessageCode implements MessageCode {
    CANNOT_RESOLVE_PATH,
    IO_ERROR,
    NOT_ENOUGH_SPACE,
    PATH_ALREADY_EXISTS,
    PATH_IS_DIRECTORY,
    PATH_IS_EMPTY,
    PATH_MAX_DEPTH,
    PATH_MAX_LENGTH,
    PATH_NOT_EMPTY,
    PATH_NOT_FOUND,
    INVALID_PATH,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
