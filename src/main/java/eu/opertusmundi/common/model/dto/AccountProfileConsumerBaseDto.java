package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileConsumerBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vat;

}
