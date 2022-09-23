package eu.opertusmundi.common.service.contract;

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
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractQuery;
import eu.opertusmundi.common.repository.contract.MasterContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractDraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;

@Service
@Transactional
public class DefaultProviderTemplateContractService implements ProviderTemplateContractService {

    private final ContractParametersFactory                 contractParametersFactory;
    private final ProviderTemplateContractRepository        providerContractRepository;
    private final ProviderTemplateContractDraftRepository   providerContractDraftRepository;
    private final ProviderTemplateContractHistoryRepository historyRepository;
    private final PdfContractGeneratorService               pdfService;
    private final MasterContractHistoryRepository           masterContractHistoryRepository;

    @Autowired
    public DefaultProviderTemplateContractService(
        ContractParametersFactory                 contractParametersFactory,
        ProviderTemplateContractRepository        providerContractRepository,
        ProviderTemplateContractDraftRepository   providerContractDraftRepository,
        ProviderTemplateContractHistoryRepository historyRepository,
        PdfContractGeneratorService               pdfService,
        MasterContractHistoryRepository           masterContractHistoryRepository
    ) {
        this.contractParametersFactory       = contractParametersFactory;
        this.providerContractRepository      = providerContractRepository;
        this.providerContractDraftRepository = providerContractDraftRepository;
        this.historyRepository               = historyRepository;
        this.pdfService                      = pdfService;
        this.masterContractHistoryRepository = masterContractHistoryRepository;
    }


    @Override
    public PageResultDto<ProviderTemplateContractDto> findAllDrafts(
        UUID providerKey,
        int page,
        int size,
        EnumProviderContractSortField orderBy,
        EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy.getValue()));

        final Page<ProviderTemplateContractDto> p = this.providerContractDraftRepository.findAllObjects(providerKey, pageRequest);

        final long count = p.getTotalElements();
        final List<ProviderTemplateContractDto> records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderTemplateContractDto> result = PageResultDto.of(page, size, records, count);

        return result;
    }

    @Override
    public ProviderTemplateContractDto findOneDraft(UUID providerKey, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.providerContractDraftRepository.findOneObject(providerKey, draftKey).orElse(null);

        return result;
    }

    @Override
    public ProviderTemplateContractDto updateDraft(ProviderTemplateContractCommandDto command) {
        final ProviderTemplateContractDto result = this.providerContractDraftRepository.saveFrom(command);

        return result;
    }

    @Override
    public ProviderTemplateContractDto deleteDraft(Integer providerId, UUID draftKey) {
        final ProviderTemplateContractDto result = providerContractDraftRepository.deleteByKey(providerId, draftKey);

        return result;
    }

    @Override
    public ProviderTemplateContractDto publishDraft(UUID providerKey, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.historyRepository.publishDraft(providerKey, draftKey);

        return result;
    }

    @Override
    public PageResultDto<ProviderTemplateContractDto> findAll(ProviderTemplateContractQuery query) {
        final Direction   direction   = query.getOrder() == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), Sort.by(direction, query.getOrderBy().getValue()));

        final Page<ProviderTemplateContractDto> p = this.providerContractRepository.findAllObjects(
            query.getProviderKey(), pageRequest
        );

        final long                                       count   = p.getTotalElements();
        final List<ProviderTemplateContractDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderTemplateContractDto> result  = PageResultDto.of(query.getPage(), query.getSize(), records, count);

        return result;
    }

    @Override
    public Optional<ProviderTemplateContractDto> findOneByKey(Integer providerId, UUID templateKey) {
        final ProviderTemplateContractDto result = this.providerContractRepository.findOneObject(providerId, templateKey).orElse(null);

        return Optional.ofNullable(result);
    }

    @Override
    public ProviderTemplateContractDto createFromMasterContract(UUID providerKey, UUID templateKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.providerContractDraftRepository.createFromHistory(providerKey, templateKey);

        return result;
    }

    @Override
    public ProviderTemplateContractDto deactivate(UUID providerKey, UUID templateKey, boolean force) {
        final ProviderTemplateContractDto result = this.historyRepository.deactivate(providerKey, templateKey, force);

        return result;
    }

    @Override
    public byte[] print(ProviderContractCommand command) {
        byte[] contractByteArray = null;
        try {
            final ContractParametersDto parameters = contractParametersFactory.createWithPlaceholderData();

            contractByteArray = pdfService.renderProviderPDF(parameters, command);
        } catch (final ContractServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ContractServiceException(ContractMessageCode.ERROR, ex);
        }
        return contractByteArray;
    }

    @Override
    public void createDefaultContract(UUID providerKey) {
        final var defaultMasterContract = masterContractHistoryRepository.findDefaultContract().orElse(null);
        if (defaultMasterContract == null) {
            // Master contract may not exist
            return;
        }

        final var providerDefaultContract = this.historyRepository.findDefaultProviderContract(providerKey).orElse(null);
        if (providerDefaultContract != null) {
            // If provider contract template has not changed, do not update
            // contract
            if (defaultMasterContract.getId() == providerDefaultContract.getTemplate().getId()) {
                return;
            }
            // Deactivate existing default contract
            this.deactivate(providerKey, providerDefaultContract.getKey(), true);
        }

        // Stage new provider default contract as a draft and then publish
        final var providerDefaultContractDraft = this.providerContractDraftRepository.createDefaultContractDraft(providerKey);

        this.publishDraft(providerKey, providerDefaultContractDraft.getKey());
    }

    @Override
    public ProviderTemplateContractDto acceptDefaultContract(UUID providerKey) {
        return this.historyRepository.acceptDefaultContract(providerKey);
    }
}
