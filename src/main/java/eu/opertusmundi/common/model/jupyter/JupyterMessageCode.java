package eu.opertusmundi.common.model.jupyter;

import eu.opertusmundi.common.model.MessageCode;

public enum JupyterMessageCode implements MessageCode {
    API_ERROR,
    PROFILE_NOT_FOUND,
    SERVER_RUNNING,
    SERVER_NOT_RUNNING,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
