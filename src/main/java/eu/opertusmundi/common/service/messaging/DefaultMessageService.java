package eu.opertusmundi.common.service.messaging;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.message.EnumMessageView;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.client.ClientContactDto;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadDto;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;
import eu.opertusmundi.common.model.message.server.ServerMessageCommandDto;
import eu.opertusmundi.common.model.message.server.ServerMessageDto;
import eu.opertusmundi.common.model.message.server.ServerMessageThreadDto;
import eu.opertusmundi.common.model.message.server.ServerNotificationDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.HelpdeskAccountRepository;

@Service
public class DefaultMessageService implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageService.class);

    @Autowired
    private ObjectProvider<MessageServiceFeignClient> messageClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HelpdeskAccountRepository helpdeskAccountRepository;

    @Override
    public Long countUnassignedMessages() {
        try {
            final Long result = this.messageClient.getObject().countUnassignedMessages().getBody().getResult();

            return result;
        } catch (final Exception ex) {
            throw new ServiceException("Failed to count unassigned messages", ex);
        }
    }

    @Override
    public PageResultDto<ClientMessageDto> findUnassignedMessages(
        int page, int size, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) throws ServiceException {
        try {
            final ResponseEntity<RestResponse<PageResultDto<ServerMessageDto>>> e = this.messageClient.getObject().getHelpdeskInbox(
                page, size, dateFrom, dateTo, read
            );

            final RestResponse<PageResultDto<ServerMessageDto>> serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to load unassigned messages [message={}]", message);
                throw new ServiceException("Failed to load messages");
            }

            final PageResultDto<ClientMessageDto> result = serviceResponse.getResult().convert(ClientMessageDto::from);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Failed to load unassigned message", ex);
            throw new ServiceException("Failed to load unassigned messages");
        }
    }

    @Override
    public Long countUserNewMessages(UUID userKey) {
        try {
            final Long result = this.messageClient.getObject().countUserNewMessages(userKey).getBody().getResult();

            return result;
        } catch (final Exception ex) {
            throw new ServiceException("Failed to count new messages", ex);
        }
    }

    @Override
    public PageResultDto<ClientMessageDto> findMessages(
        UUID userKey, int page, int size, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageView view, UUID contactKey
    ) throws ServiceException {
        try {
            final ResponseEntity<RestResponse<PageResultDto<ServerMessageDto>>> e = this.messageClient.getObject().findMessages(
                userKey, page, size, dateFrom, dateTo, view, contactKey
            );

            final RestResponse<PageResultDto<ServerMessageDto>> serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to load messages [userKey={}, message={}]", userKey, message);
                throw new ServiceException("Failed to load messages");
            }

            final PageResultDto<ClientMessageDto> result = serviceResponse.getResult().convert(ClientMessageDto::from);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to load messages [userKey=%s]", userKey), ex);
            throw new ServiceException("Failed to load messages");
        }
    }

    @Override
    public List<ClientContactDto> findContacts(String email) {
        final List<ClientContactDto> contacts = new ArrayList<>();

        final PageRequest pageRequest = PageRequest.of(0, 20);
        final String      filter      = "%" + email + "%";

        this.accountRepository.findAllByEmailContains(filter, pageRequest).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        this.helpdeskAccountRepository.findAllByEmailContains(filter, pageRequest).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        contacts.sort((a, b) -> a.getEmail().compareTo(b.getEmail()));

        return contacts;
    }

    @Override
    public List<ClientContactDto> findContacts(List<ClientMessageDto> messages) {
        final List<UUID> contractKeys = new ArrayList<>();

        messages.stream()
            .map(i -> i.getSenderId())
            .forEach(contractKeys::add);

        messages.stream()
            .map(i -> i.getRecipientId())
            .forEach(contractKeys::add);

        return this.getContactsFromKeys(contractKeys);
    }


    @Override
    public ClientMessageDto assignMessage(UUID messageKey, UUID userKey) {
        try {
            final ResponseEntity<RestResponse<ServerMessageDto>> e = this.messageClient.getObject()
                .assignMessage(messageKey, userKey);

            final RestResponse<ServerMessageDto> serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to assign message [messageKey={}, userKey={}, message={}]", messageKey, userKey, message);
                throw new ServiceException("Failed to assign message");
            }

            final ClientMessageDto result = ClientMessageDto.from(serviceResponse.getResult());
            this.injectContracts(result);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to assign message";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public ClientMessageDto readMessage(UUID ownerKey, UUID messageKey) {
        try {
            final ResponseEntity<RestResponse<ServerMessageDto>> e = this.messageClient.getObject().readMessage(ownerKey, messageKey);

            final RestResponse<ServerMessageDto> serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to read message [messageKey={}, userKey={}, message={}]", messageKey, ownerKey, message);
                throw new ServiceException("Failed to read message");
            }

            final ClientMessageDto result = ClientMessageDto.from(serviceResponse.getResult());
            this.injectContracts(result);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to read message";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public ClientMessageThreadDto readThread(UUID ownerKey, UUID threadKey) {
        try {
            final ResponseEntity<RestResponse<ServerMessageThreadDto>> e = this.messageClient.getObject().readThread(ownerKey, threadKey);

            final RestResponse<ServerMessageThreadDto> serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to mark thread messages as read[ownerKey={}, threadKey={}, message={}]", ownerKey, threadKey, message);
                throw new ServiceException("Failed to mark thread messages as read");
            }

            final ClientMessageThreadDto result = ClientMessageThreadDto.from(serviceResponse.getResult());
            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to read message";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public void deleteAllMessages(UUID contactKey) {
        try {
            final ResponseEntity<BaseResponse> e = this.messageClient.getObject().deleteAllMessages(contactKey);

            final BaseResponse serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to delete messages [contactKey={}, message={}]", contactKey, message);
                throw new ServiceException("Failed to delete messages");
            }
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to delete messages";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public ClientMessageDto sendMessage(UUID senderKey, UUID recipientKey, ClientMessageCommandDto clientMessage) {
        try {
            final ServerMessageCommandDto serverMessage = new ServerMessageCommandDto();

            serverMessage.setSender(senderKey);
            serverMessage.setRecipient(recipientKey);
            serverMessage.setSubject(clientMessage.getSubject());
            serverMessage.setText(clientMessage.getText());

            final ResponseEntity<RestResponse<ServerMessageDto>> e               = this.messageClient.getObject().sendMessage(serverMessage);
            final RestResponse<ServerMessageDto>                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to send message [senderKey={}, recipientKey={}, message={}]", senderKey, recipientKey, message);
                throw new ServiceException("Failed to send message");
            }

            final ClientMessageDto result = ClientMessageDto.from(serviceResponse.getResult());
            this.injectContracts(result);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to send message";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public ClientMessageDto replyToMessage(UUID senderKey, UUID threadKey, ClientMessageCommandDto clientMessage) {
        try {
            final ServerMessageCommandDto serverMessage = new ServerMessageCommandDto();

            serverMessage.setSender(senderKey);
            serverMessage.setThread(threadKey);
            serverMessage.setText(clientMessage.getText());

            final ResponseEntity<RestResponse<ServerMessageDto>> e               = this.messageClient.getObject().sendMessage(serverMessage);
            final RestResponse<ServerMessageDto>                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to reply to message [senderKey={}, threadKey={}, message={}]", senderKey, threadKey, message);
                throw new ServiceException("Failed to reply to message");
            }

            final ClientMessageDto result = ClientMessageDto.from(serviceResponse.getResult());
            this.injectContracts(result);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to reply to message";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public ClientMessageThreadDto getMessageThread(UUID ownerKey, UUID threadKey) {
        try {
            final ResponseEntity<RestResponse<ServerMessageThreadDto>> e               = this.messageClient.getObject()
                .getMessageThread(ownerKey, threadKey);
            final RestResponse<ServerMessageThreadDto>                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to load message thread [ownerKey={}, threadKey={}, message={}]", ownerKey, threadKey, message);
                throw new ServiceException("Failed to load message thread");
            }

            final ClientMessageThreadDto result = serviceResponse.getResult() == null
                ? null
                : ClientMessageThreadDto.from(serviceResponse.getResult());
            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to load message thread";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public PageResultDto<ClientNotificationDto> findNotifications(
        UUID ownerKey, Integer pageIndex, Integer pageSize,
        ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    ) {
        try {
            final ResponseEntity<RestResponse<PageResultDto<ServerNotificationDto>>> e               = this.messageClient.getObject()
                .findNotifications(pageIndex, pageSize, ownerKey, dateFrom, dateTo, read, orderBy, order);
            final RestResponse<PageResultDto<ServerNotificationDto>>                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to load notifications [ownerKey={}, message={}]", ownerKey, message);
                throw new ServiceException("Failed to load notifications");
            }

            final PageResultDto<ClientNotificationDto> result = serviceResponse.getResult().convert(n -> {
                return new ClientNotificationDto(n);
            });

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to load message thread";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    @Override
    public void readNotification(UUID recipientKey, UUID key) {
        try {
            final ResponseEntity<BaseResponse> e               = this.messageClient.getObject().readNotification(recipientKey, key);
            final BaseResponse                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to read notification [recipientKey={}, key={}, message={}]", recipientKey, key, message);
                throw new ServiceException("Failed to read notification");
            }
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to read notification [recipientKey=%s, key=%s]", recipientKey, key), ex);
            throw new ServiceException("Failed to read notification");
        }
    }

    @Override
    public void readAllNotifications(UUID recipientKey) {
        try {
            final ResponseEntity<BaseResponse> e               = this.messageClient.getObject().readAllNotifications(recipientKey);
            final BaseResponse                 serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to read notifications [recipientKey={}, message={}]", recipientKey, message);
                throw new ServiceException("Failed to read notifications");
            }

        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to read notifications [recipientKey=%s]", recipientKey), ex);
            throw new ServiceException("Failed to read notifications");
        }
    }

    @Override
    public void deleteAllNotifications(UUID recipientKey) {
        try {
            final ResponseEntity<BaseResponse> e = this.messageClient.getObject().deleteAllNotifications(recipientKey);

            final BaseResponse serviceResponse = e.getBody();

            if (!serviceResponse.getSuccess()) {
                final String message = serviceResponse.getMessages().get(0).getDescription();
                logger.error("Failed to delete notifications [recipientKey={}, message={}]", recipientKey, message);
                throw new ServiceException("Failed to delete notifications");
            }
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = "Failed to delete notifications";
            logger.error(message, ex);
            throw new ServiceException(message);
        }
    }

    private List<ClientContactDto> getContactsFromKeys(List<UUID> keys) {
        final List<ClientContactDto> contacts = new ArrayList<>();

        final List<UUID> uniqueContractKeys = keys.stream().filter(k -> k != null).distinct().collect(Collectors.toList());

        this.accountRepository.findAllByKey(uniqueContractKeys).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        this.helpdeskAccountRepository.findAllByKey(uniqueContractKeys).stream()
            .map(ClientContactDto::new)
            .forEach(contacts::add);

        return contacts;
    }

    private void injectContracts(ClientMessageDto message) {
        final List<ClientContactDto> contacts = this.getContactsFromKeys(Arrays.asList(message.getSenderId(), message.getRecipientId()));

        message.setRecipient(contacts.stream().filter(c -> c.getId().equals(message.getRecipientId())).findFirst().orElse(null));
        message.setSender(contacts.stream().filter(c -> c.getId().equals(message.getSenderId())).findFirst().orElse(null));
    }
}
