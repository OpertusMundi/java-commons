package eu.opertusmundi.common.service.messaging;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.ContactFormEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.message.ContactFormCommandDto;
import eu.opertusmundi.common.model.message.ContactFormDto;
import eu.opertusmundi.common.model.message.EnumContactFormSortField;
import eu.opertusmundi.common.model.message.EnumContactFormStatus;
import eu.opertusmundi.common.repository.ContactFormRepository;

@Service
public class DefaultContactFormService implements ContactFormService {

    @Autowired
    private ContactFormRepository contactFormRepository;

    public PageResultDto<ContactFormDto> find(
        String email, EnumContactFormStatus status, ZonedDateTime dateFrom, ZonedDateTime dateTo,
        Integer pageIndex, Integer pageSize, EnumContactFormSortField sortBy, EnumSortingOrder sortOrder
    ) {
        final Direction direction = sortOrder == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;

        final PageRequest             pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, sortBy.getValue()));
        final Page<ContactFormEntity> page        = this.contactFormRepository.findAll(email, status, dateFrom, dateTo, pageRequest);

        final long                   count = page.getTotalElements();
        final List<ContactFormDto> records = page.getContent().stream()
            .map(f -> f.toDto(true))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public Long countPendingForms() {
        var result = this.contactFormRepository.countFormsWithStatus(EnumContactFormStatus.PENDING);
        return result;
    }

    @Override
    public ContactFormDto create(ContactFormCommandDto command) {
        final var result = contactFormRepository.create(command);
        return result;
    }

    @Override
    public ContactFormDto completeForm(UUID formKey) {
        final var result = contactFormRepository.completeForm(formKey);
        return result;
    }

}
