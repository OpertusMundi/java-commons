package eu.opertusmundi.common.model.catalogue.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogueResponse<R> {

    private R result;

    private boolean success;

    private CatalogueMessage message;

}
