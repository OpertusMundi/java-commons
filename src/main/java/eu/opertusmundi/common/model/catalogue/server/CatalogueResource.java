package eu.opertusmundi.common.model.catalogue.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CatalogueResource {

    private String id;
    private String category;
    private String value;
    private String format;

}
