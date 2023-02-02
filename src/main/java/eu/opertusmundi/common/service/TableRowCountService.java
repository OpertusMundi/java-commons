package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;

public interface TableRowCountService {

    long countRows(CatalogueItemDetailsDto asset, String[] nutCodes);
}
