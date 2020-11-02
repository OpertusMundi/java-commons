package eu.opertusmundi.common.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCommandDto extends AddressBaseDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private UUID key;

}
