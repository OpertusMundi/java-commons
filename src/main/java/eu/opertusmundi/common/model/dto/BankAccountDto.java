package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDto extends BankAccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private AddressDto ownerAddress;

    @JsonIgnore
    private String tag;

}
