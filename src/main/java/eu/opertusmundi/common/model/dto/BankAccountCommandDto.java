package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountCommandDto extends BankAccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull
    private AddressCommandDto ownerAddress;

    @JsonIgnore
    private String tag;

}
