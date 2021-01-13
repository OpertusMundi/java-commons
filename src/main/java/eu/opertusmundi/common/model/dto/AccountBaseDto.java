package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountBaseDto implements Serializable {

    public AccountBaseDto(boolean active, boolean blocked) {
        super();
        this.active  = active;
        this.blocked = blocked;
    }

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    protected boolean active;

    @JsonIgnore
    protected boolean blocked;

}