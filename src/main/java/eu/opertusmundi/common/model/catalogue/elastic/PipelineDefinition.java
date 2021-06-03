package eu.opertusmundi.common.model.catalogue.elastic;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineDefinition {

    private String name;

    private String definition;

    public boolean isValid() {
        return !StringUtils.isBlank(name) && !StringUtils.isBlank(definition);
    }
}
