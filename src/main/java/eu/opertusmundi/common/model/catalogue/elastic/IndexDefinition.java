package eu.opertusmundi.common.model.catalogue.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexDefinition {

    private String name;

    private String settings;

    private String mappings;
    
}
