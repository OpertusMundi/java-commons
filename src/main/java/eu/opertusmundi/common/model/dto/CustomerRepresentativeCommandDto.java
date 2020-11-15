package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.Valid;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRepresentativeCommandDto extends CustomerRepresentativeBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private AddressCommandDto address;

}
