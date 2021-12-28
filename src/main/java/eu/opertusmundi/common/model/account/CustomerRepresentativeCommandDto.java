package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRepresentativeCommandDto extends CustomerRepresentativeBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull
    private AddressCommandDto address;

}
