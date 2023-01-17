package eu.opertusmundi.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountTicketCommandDto;
import eu.opertusmundi.common.model.account.AccountTicketDto;
import eu.opertusmundi.common.model.account.EnumTicketStatus;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.repository.AccountTicketRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.service.messaging.MessageService;

@Service
public class DefaultTicketService implements TicketService {

    private final AccountTicketRepository accountTicketRepository;
    private final MessageService          messageService;
    private final OrderRepository         orderRepository;
    private final PayInRepository         payInRepository;

    @Autowired
    public DefaultTicketService(
        AccountTicketRepository accountTicketRepository,
        MessageService          messageService,
        OrderRepository         orderRepository,
        PayInRepository         payInRepository
    ) {
        this.accountTicketRepository = accountTicketRepository;
        this.messageService          = messageService;
        this.orderRepository         = orderRepository;
        this.payInRepository         = payInRepository;
    }

    @Override
    public PageResultDto<AccountTicketDto> findAll(
        UUID ownerKey, int page, int size, Set<EnumTicketStatus> status, boolean includeDetails
    ) throws ServiceException {
        final Direction   direction   = Direction.DESC;
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, "updatedAt"));

        final var result = this.accountTicketRepository
            .findAll(ownerKey, null /* assigneeKey */, status, pageRequest)
            .map(e -> e.toDto(includeDetails));

        return PageResultDto.of(page, size, result.getContent(), result.getTotalElements());
    }

    @Override
    public AccountTicketDto findOne(UUID userKey, UUID ticketKey, boolean includeDetails) throws ServiceException {
        final var result = this.accountTicketRepository
            .findOneByKey(userKey, ticketKey)
            .map(e -> e.toDto(includeDetails))
            .orElse(null);

        return result;
    }

    @Override
    @Transactional
    public AccountTicketDto create(AccountTicketCommandDto ticketCommand) throws ServiceException {
        final var attributes = this.getAttributes(ticketCommand);
        final var subject    = attributes.get("subject");

        ticketCommand.setSubject(subject);
        var ticket = this.accountTicketRepository.create(ticketCommand);

        attributes.put("ticket", ticket.getKey().toString());
        final var messageCommand = ClientMessageCommandDto.of(subject, ticketCommand.getText(), attributes);
        final var message        = this.messageService.sendMessage(ticketCommand.getUserKey(), null, messageCommand);

        ticket = this.accountTicketRepository.setTicketMessageThread(ticketCommand.getUserKey(), ticket.getKey(), message.getThread());

        return ticket;
    }

    @Override
    public AccountTicketDto assignTicket(UUID userKey, UUID ticketKey, UUID assigneeKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccountTicketDto reply(UUID userKey, UUID ticketKey, String message) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccountTicketDto setStatus(UUID userKey, UUID ticketKey, EnumTicketStatus status) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    private Map<String, String> getAttributes(AccountTicketCommandDto c) throws ServiceException {
        final var result = new HashMap<String, String>();
        result.put("type", c.getType().toString());
        result.put("userKey", c.getUserKey().toString());
        result.put("resourceKey", c.getResourceKey().toString());

        switch (c.getType()) {
            case ORDER : {
                final var order = this.orderRepository
                    .findObjectByKeyAndConsumerAndStatusNotCreated(c.getUserKey(), c.getResourceKey())
                    .orElse(null);
                if (order == null) {
                    throw new ServiceException(BasicMessageCode.RecordNotFound, "Order was not found");
                }
                result.put("subject", String.format("Order %s", order.getReferenceNumber()));
                result.put("referenceNumber", order.getReferenceNumber());
                break;
            }
            case PAYIN : {
                final var payIn = this.payInRepository
                    .findOneByConsumerKeyAndKey(c.getUserKey(), c.getResourceKey())
                    .orElse(null);
                if (payIn == null) {
                    throw new ServiceException(BasicMessageCode.RecordNotFound, "Order was not found");
                }
                result.put("subject", String.format("Payment %s", payIn.getReferenceNumber()));
                result.put("referenceNumber", payIn.getReferenceNumber());
                break;
            }
            default :
                throw new ServiceException(BasicMessageCode.InternalServerError, "Ticket type is not supported");
        }

        return result;
    }

}
