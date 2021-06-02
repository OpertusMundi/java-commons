package eu.opertusmundi.common.model.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterTemplateContractCommandDto {

    @JsonIgnore
    private Integer id;

}
