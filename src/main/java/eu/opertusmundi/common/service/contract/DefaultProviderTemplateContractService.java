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
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractDraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;

@Service
@Transactional
public class DefaultProviderTemplateContractService implements ProviderTemplateContractService {

	@Autowired
	private PdfContractGeneratorService pdfService;

	@Autowired
	private ContractParametersFactory contractParametersFactory;

    @Autowired
    private ProviderTemplateContractDraftRepository draftRepository;

    @Autowired
    private  ProviderTemplateContractHistoryRepository historyRepository;

    @Autowired
    private  ProviderTemplateContractRepository contractRepository;

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

        final Page<ProviderTemplateContractDto> p = this.draftRepository.findAllObjects(providerKey, pageRequest);

        final long count = p.getTotalElements();
        final List<ProviderTemplateContractDto> records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderTemplateContractDto> result = PageResultDto.of(page, size, records, count);

        return result;
    }

    @Override
    public ProviderTemplateContractDto findOneDraft(UUID providerKey, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.draftRepository.findOneObject(providerKey, draftKey).orElse(null);

        return result;
    }

    @Override
    public ProviderTemplateContractDto updateDraft(ProviderTemplateContractCommandDto command) {
        final ProviderTemplateContractDto result = this.draftRepository.saveFrom(command);

        return result;
    }

    @Override
    public ProviderTemplateContractDto deleteDraft(Integer providerId, UUID draftKey) {
        final ProviderTemplateContractDto result = draftRepository.deleteByKey(providerId, draftKey);

        return result;
    }

    @Override
    public ProviderTemplateContractDto publishDraft(Integer providerId, UUID draftKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.historyRepository.publishDraft(providerId, draftKey);

        return result;
    }

    @Override
    public PageResultDto<ProviderTemplateContractDto> findAll(ProviderTemplateContractQuery query) {
        final Direction   direction   = query.getOrder() == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), Sort.by(direction, query.getOrderBy().getValue()));

        final Page<ProviderTemplateContractDto> p = this.contractRepository.findAllObjects(
            query.getProviderKey(), pageRequest
        );

        final long                                       count   = p.getTotalElements();
        final List<ProviderTemplateContractDto>          records = p.stream().collect(Collectors.toList());
        final PageResultDto<ProviderTemplateContractDto> result  = PageResultDto.of(query.getPage(), query.getSize(), records, count);

        return result;
    }

    @Override
    public Optional<ProviderTemplateContractDto> findOneByKey(Integer providerId, UUID templateKey) {
        final ProviderTemplateContractDto result = this.contractRepository.findOneObject(providerId, templateKey).orElse(null);

        return Optional.ofNullable(result);
    }

    @Override
    public ProviderTemplateContractDto createFromMasterContract(int userId, UUID providerKey, UUID templateKey) throws ApplicationException {
        final ProviderTemplateContractDto result = this.draftRepository.createFromHistory(
            userId, providerKey, templateKey
        );

        return result;
    }

    @Override
    public ProviderTemplateContractDto deactivate(Integer providerId, UUID templateKey) {
        final ProviderTemplateContractDto result = this.historyRepository.deactivate(providerId, templateKey);

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

}
