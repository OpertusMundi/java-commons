package eu.opertusmundi.common.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileProviderCommandDto extends AccountProfileProviderBaseDto {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

}
