package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.helpdesk.DeactivateContractResult;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.helpdesk.PublishContractResult;
import eu.opertusmundi.common.repository.contract.MasterContractDraftRepository;
import eu.opertusmundi.common.repository.contract.MasterContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.MasterContractRepository;

@Service
public class DefaultMasterTemplateContractService implements MasterTemplateContractService {

    @Autowired
    private MasterContractDraftRepository draftRepository;

    @Autowired
    private MasterContractHistoryRepository historyRepository;

    @Autowired
    private MasterContractRepository contractRepository;
    
    @Autowired
    private PdfContractGeneratorService pdfService;

    @Autowired
    private ContractParametersFactory contractParametersFactory;

    @Override
    public PageResultDto<MasterContractHistoryDto> findAllHistory(MasterContractQueryDto query) {
        final Direction   direction   = query.getOrder() == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), Sort.by(direction, query.getOrderBy().getValue()));

        final Page<MasterContractHistoryDto> p = this.historyRepository.findHistoryObjects(
            query.getTitle(), query.getStatus(), pageRequest
        );

        final long                                    count   = p.getTotalElements();
        final List<MasterContractHistoryDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<MasterContractHistoryDto> result  = PageResultDto.of(query.getPage(), query.getSize(), records, count);

        return result;
    }

    @Override
    public PageResultDto<MasterContractDto> findAll(MasterContractQueryDto query) {
        final Direction   direction   = query.getOrder() == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), Sort.by(direction, query.getOrderBy().getValue()));

        final Page<MasterContractDto> p = this.contractRepository.findAllObjects(
            query.getTitle(), pageRequest
        );

        final long                             count   = p.getTotalElements();
        final List<MasterContractDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<MasterContractDto> result  = PageResultDto.of(query.getPage(), query.getSize(), records, count);

        return result;
    }

    @Override
    public Optional<MasterContractDto> findOneById(int id) {
        final MasterContractDto result = this.contractRepository.findOneObject(id).orElse(null);

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<MasterContractDto> findOneByKey(UUID key) {
        final MasterContractDto result = this.contractRepository.findOneObject(key).orElse(null);

        return Optional.ofNullable(result);
    }

    @Override
    public MasterContractDto createForTemplate(int userId, int templateId) throws ApplicationException {
        final MasterContractDto result = this.draftRepository.createFromHistory(userId, templateId);

        return result;
    }
    
    @Override
    public MasterContractDto cloneFromTemplate(int userId, int templateId) throws ApplicationException {
        final MasterContractDto result = this.draftRepository.cloneFromHistory(userId, templateId);

        return result;
    }

    @Override
    @Transactional
    public MasterContractHistoryDto deactivate(int id) throws ApplicationException {
        final DeactivateContractResult result = this.historyRepository.deactivate(id);

        this.contractRepository.deleteById(result.getContractId());
        
        return result.getContract();
    }

    @Override
    public PageResultDto<MasterContractDto> findAllDrafts(
        int page,
        int size,
        EnumMasterContractSortField orderBy,
        EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy.getValue()));

        final Page<MasterContractDto> p = this.draftRepository.findAllObjects(pageRequest);

        final long count = p.getTotalElements();
        final List<MasterContractDto> records = p.stream().collect(Collectors.toList());
        final PageResultDto<MasterContractDto> result = PageResultDto.of(page, size, records, count);

        return result;
    }

    @Override
    public MasterContractDto findOneDraft(int id) throws ApplicationException  {
        final MasterContractDto result = this.draftRepository.findOneObject(id).orElse(null);

        return result;
    }

    @Override
    public MasterContractDto updateDraft(MasterContractCommandDto command) throws ApplicationException {
        final MasterContractDto result = this.draftRepository.saveFrom(command);

        return result;
    }

    @Override
    public void deleteDraft(int id) throws ApplicationException {
        this.draftRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MasterContractDto publishDraft(int id) throws ApplicationException {
        final PublishContractResult result = this.historyRepository.publishDraft(id);

        this.draftRepository.deleteById(id);
        if (result.getPreviousContractId() != null) {
            this.contractRepository.deleteById(result.getPreviousContractId());
        }

        return result.getContract();
    }

    @Override
    public byte[] print(int masterContractId) throws IOException {

        final ContractParametersDto parameters = contractParametersFactory.createWithPlaceholderData();
        
        return pdfService.renderMasterPDF(parameters, masterContractId);
    }

}
