package eu.opertusmundi.common.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractHistoryDto;


@Entity(name = "ProviderContractHistoryView")
@Table(
    schema = "contract", name = "`v_provider_contract`"
)
public class ProviderTemplateContractHistoryViewEntity extends ProviderTemplateContractHistoryBaseEntity {

    public ProviderTemplateContractHistoryDto toDto(boolean includeDetails) {
    	final ProviderTemplateContractHistoryDto c = new ProviderTemplateContractHistoryDto();

        c.setCreatedAt(createdAt);
        c.setDefaultContract(defaultContract);
        c.setDefaultContractAccepted(defaultContractAccepted);
        c.setDefaultContractAcceptedAt(getDefaultContractAcceptedAt());
        c.setId(id);
        c.setKey(key);
        c.setModifiedAt(modifiedAt);
        c.setOwner(owner.toSimpleDto());
        c.setStatus(status);
        c.setSubtitle(subtitle);
        c.setTitle(title);
        c.setVersion(version);

        return c;
    }

}
