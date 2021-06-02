package eu.opertusmundi.common.model.catalogue.elastic;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransformDefinition {

    private String name;

    private String sourceIndex;

    private String destIndex;

    public boolean isValid() {
        return !StringUtils.isBlank(name) && !StringUtils.isBlank(sourceIndex) && !StringUtils.isBlank(destIndex);
    }
}
