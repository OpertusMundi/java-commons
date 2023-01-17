package eu.opertusmundi.common.service;

import java.util.Set;
import java.util.UUID;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountTicketCommandDto;
import eu.opertusmundi.common.model.account.AccountTicketDto;
import eu.opertusmundi.common.model.account.EnumTicketStatus;

public interface TicketService {

    PageResultDto<AccountTicketDto> findAll(
        UUID userKey, int page, int size, Set<EnumTicketStatus> status, boolean includeDetails
    ) throws ServiceException;

    AccountTicketDto findOne(UUID userKey, UUID ticketKey, boolean includeDetails) throws ServiceException;

    AccountTicketDto create(AccountTicketCommandDto command) throws ServiceException;

    AccountTicketDto assignTicket(UUID userKey, UUID ticketKey, UUID assigneeKey);

    AccountTicketDto reply(UUID userKey, UUID ticketKey, String message) throws ServiceException;

    AccountTicketDto setStatus(UUID userKey, UUID ticketKey, EnumTicketStatus status) throws ServiceException;

}
