package eu.opertusmundi.common.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.repository.contract.MasterContractHistoryRepository;

@Service
public class DefaultMasterTemplateContractValidator {

    @Autowired
    private MasterContractHistoryRepository historyRepository;

    public void validateHistory(int id) {
        final MasterContractHistoryEntity contract = this.historyRepository.findOneByActiveAndId(id).orElse(null);
        if (contract == null) {
            throw ApplicationException.fromMessage(ContractMessageCode.CONTRACT_NOT_FOUND, "Selected contract does not exist");
        }

        final List<MasterSectionHistoryEntity> sections = contract.getSections();
        for (final MasterSectionHistoryEntity s : sections) {
            var dynamic  = s.isDynamic();
            var optional = s.isOptional();

            if (dynamic && optional) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.DEFAULT_MASTER_CONTRACT_SECTION_INVALID_CONFIG,
                    "A section can be either dynamic or optional"
                );
            }
            if (optional && s.getOptions().size() != 1) {
                throw ApplicationException.fromMessage(
                    ContractMessageCode.DEFAULT_MASTER_CONTRACT_SECTION_INVALID_CONFIG,
                    "An optional section must have exactly one option"
                );
            }
        }
    }
}
