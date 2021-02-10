package eu.opertusmundi.common.model.catalogue.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CatalogueAdditionalResource {

    private String id;
    private String name;
    private String type;
    private String value;

}
