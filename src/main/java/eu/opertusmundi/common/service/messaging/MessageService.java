package eu.opertusmundi.common.service.messaging;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.message.EnumMessageView;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.client.ClientContactDto;
import eu.opertusmundi.common.model.message.client.ClientMessageCommandDto;
import eu.opertusmundi.common.model.message.client.ClientMessageDto;
import eu.opertusmundi.common.model.message.client.ClientMessageThreadDto;
import eu.opertusmundi.common.model.message.client.ClientNotificationDto;

public interface MessageService {

    Long countUnassignedMessages();

    PageResultDto<ClientMessageDto> findUnassignedMessages(
        int page, int size, ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read
    ) throws ServiceException;

    Long countUserNewMessages(UUID userKey);

    PageResultDto<ClientMessageDto> findMessages(
        UUID userKey, int page, int size, ZonedDateTime dateFrom, ZonedDateTime dateTo, EnumMessageView view, UUID contactKey
    ) throws ServiceException;

    List<ClientContactDto> findContacts(String email);

    List<ClientContactDto> findContacts(List<ClientMessageDto> messages);

    ClientMessageDto assignMessage(UUID messageKey, UUID userKey);

    ClientMessageDto readMessage(UUID ownerKey, UUID messageKey);

    ClientMessageThreadDto readThread(UUID ownerKey, UUID threadKey);

    void deleteAllMessages(UUID contactKey);

    ClientMessageDto sendMessage(UUID senderKey, UUID recipientKey, ClientMessageCommandDto clientMessage);

    ClientMessageDto replyToMessage(UUID senderKey, UUID threadKey, ClientMessageCommandDto clientMessage);

    ClientMessageThreadDto getMessageThread(UUID ownerKey, UUID threadKey);

    PageResultDto<ClientNotificationDto> findNotifications(
        UUID ownerKey, Integer pageIndex, Integer pageSize,
        ZonedDateTime dateFrom, ZonedDateTime dateTo, Boolean read,
        EnumNotificationSortField orderBy, EnumSortingOrder order
    );

    void readNotification(UUID recipientKey, UUID key);

    void readAllNotifications(UUID recipientKey);

    void deleteAllNotifications(UUID recipientKey);
}
