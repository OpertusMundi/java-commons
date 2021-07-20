package eu.opertusmundi.common.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;

@Entity(name = "ContractHistoryView")
@Table(schema = "contract", name = "`v_master_contract`")
@Immutable
public class MasterContractHistoryViewEntity extends MasterContractHistoryBaseEntity {

    public MasterContractHistoryDto toDto(boolean includeDetails) {
        final MasterContractHistoryDto c = new MasterContractHistoryDto();

        c.setCreatedAt(createdAt);
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