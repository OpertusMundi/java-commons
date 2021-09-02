package eu.opertusmundi.common.model.contract;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ContractSectionOptionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String body;

    @Getter
    @Setter
    private String bodyHtml;

    @Getter
    @Setter
    private List<ContractSectionSubOptionDto> subOptions;

    @Getter
    @Setter
    private String summary;

    @Getter
    @Setter
    private EnumIcon icon;

    @Getter
    @Setter
    private String shortDescription;

}
