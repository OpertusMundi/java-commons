package eu.opertusmundi.common.service.messaging;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.message.ContactFormCommandDto;
import eu.opertusmundi.common.model.message.ContactFormDto;
import eu.opertusmundi.common.model.message.EnumContactFormSortField;
import eu.opertusmundi.common.model.message.EnumContactFormStatus;

public interface ContactFormService {

    ContactFormDto create(ContactFormCommandDto command);

    PageResultDto<ContactFormDto> find(
        String email, EnumContactFormStatus status, ZonedDateTime dateFrom, ZonedDateTime dateTo,
        Integer pageIndex, Integer pageSize, EnumContactFormSortField sortBy, EnumSortingOrder sortOrder
    );
    
    Long countPendingForms();
    
    ContactFormDto completeForm(UUID formKey);
}
