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

    /**
     * Update default contracts for the provider with the given key
     *
     * <p>
     * The method will initially select all active master and provider default
     * contracts.
     * <p>
     * The provider contracts for which the template id is not present in the
     * master contract collection will be deactivated.
     * <p>
     * Finally, a new provider contract will be created for every master
     * contract whose identifier is not assigned as a template id to any
     * of the existing provider contracts.
     */
    @Override
    public void updateDefaultContracts(UUID providerKey) {
        final var masterContracts   = masterContractHistoryRepository.findActiveDefaultContracts();
        final var masterContractId  = masterContracts.stream().map(c -> c.getId()).toList();
        final var providerContracts = this.historyRepository.findDefaultProviderContracts(providerKey);

        final var deactivatedContracts = providerContracts.stream()
            .filter(providerContract -> !masterContractId.contains(providerContract.getTemplate().getId()))
            .toList();

        deactivatedContracts.stream().forEach(c -> {
            this.deactivate(providerKey, c.getKey(), true);
        });

        for (var masterContract : masterContracts) {
            var existingContract = providerContracts.stream()
                .filter(c -> c.getTemplate().getId() == masterContract.getId()).findFirst()
                .orElse(null);
            if (existingContract != null) {
                continue;
            }

            // Stage new provider default contract as a draft and then publish
            final var providerDefaultContractDraft = this.providerContractDraftRepository.createDefaultContractDraft(providerKey, masterContract.getKey());

            this.publishDraft(providerKey, providerDefaultContractDraft.getKey());
        }
    }

    @Override
    public ProviderTemplateContractDto acceptDefaultContract(UUID providerKey, UUID contractKey) {
        return this.historyRepository.acceptDefaultContract(providerKey, contractKey);
    }
}
