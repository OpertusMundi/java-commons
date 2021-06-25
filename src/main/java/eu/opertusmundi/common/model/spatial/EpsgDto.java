package eu.opertusmundi.common.model.spatial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EpsgDto {

    private int code;

    private String name;

    @JsonIgnore
    private boolean active;

}
