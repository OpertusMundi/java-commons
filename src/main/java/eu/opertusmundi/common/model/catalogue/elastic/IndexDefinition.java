package eu.opertusmundi.common.model.catalogue.elastic;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexDefinition {

    private String name;

    private String settings;

    private String mappings;

    public boolean isValid() {
        return !StringUtils.isBlank(name) && !StringUtils.isBlank(settings) && !StringUtils.isBlank(mappings);
    }
}
