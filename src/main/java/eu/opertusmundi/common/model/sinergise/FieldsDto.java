package eu.opertusmundi.common.model.sinergise;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldsDto {

    @JsonInclude(Include.NON_NULL)
    private String[] include;

    @JsonInclude(Include.NON_NULL)
    private String[] exclude;

}
