package eu.opertusmundi.common.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
import eu.opertusmundi.common.model.message.client.ClientContactDto;

@Entity(name = "ContractHistoryView")
@Table(schema = "contract", name = "`v_master_contract`")
@Immutable
public class MasterContractHistoryViewEntity extends MasterContractHistoryBaseEntity {

    public MasterContractHistoryDto toDto(boolean includeDetails) {
        final MasterContractHistoryDto c = new MasterContractHistoryDto();

        c.setCreatedAt(createdAt);
        c.setDefaultContract(defaultContract);
        c.setId(id);
        c.setKey(key);
        c.setModifiedAt(modifiedAt);
        c.setOwner(owner.toSimpleDto());
        c.setStatus(status);
        c.setSubtitle(subtitle);
        c.setTitle(title);
        c.setVersion(version);
        
        if (provider != null) {
            c.setProvider(new ClientContactDto(provider));
        }

        return c;
    }

}
