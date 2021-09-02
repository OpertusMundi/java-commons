package eu.opertusmundi.common.model.contract;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractSectionSubOptionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String body;

    private String bodyHtml;
}
