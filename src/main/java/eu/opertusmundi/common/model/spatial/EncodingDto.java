package eu.opertusmundi.common.model.spatial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncodingDto {

    private String code;

    @JsonIgnore
    private boolean active;

}
