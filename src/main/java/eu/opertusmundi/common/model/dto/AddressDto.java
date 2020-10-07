package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

public class AddressDto extends AddressBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

}
