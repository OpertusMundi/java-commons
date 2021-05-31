package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public AccountBaseDto(boolean active, boolean blocked) {
        super();
        this.active  = active;
        this.blocked = blocked;
    }

    @JsonIgnore
    protected boolean active;

    @JsonIgnore
    protected boolean blocked;

}