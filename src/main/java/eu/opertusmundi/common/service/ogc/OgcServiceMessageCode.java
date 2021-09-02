package eu.opertusmundi.common.service.ogc;

import eu.opertusmundi.common.model.MessageCode;

public enum OgcServiceMessageCode implements MessageCode {
    HTTP_ERROR,
    HTTP_METHOD_NOT_SUPPORTED,
    IO_ERROR,
    RESOURCE_NOT_CREATED,
    WFS_SERVICE_ERROR,
    TYPE_NOT_SUPPORTED,
    LAYER_NOT_FOUND,
    UNKNOWN,
    URI_SYNTAX_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}

