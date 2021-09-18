package eu.opertusmundi.common.feign.client;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.feign.client.config.MessageServiceFeignClientConfiguration;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.message.EnumNotificationSortField;
import eu.opertusmundi.common.model.message.server.ServerMessageCommandDto;
import eu.opertusmundi.common.model.message.server.ServerMessageDto;
import eu.opertusmundi.common.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.common.model.message.server.ServerNotificationDto;


@FeignClient(
    name = "${opertusmundi.feign.message-service.name}",
    url = "${opertusmundi.feign.message-service.url}",
    configuration = MessageServiceFeignClientConfiguration.class
)
public interface MessageServiceFeignClient {

    /**
     * Get helpdesk unassigned messages
     *
     * @param pageIndex
     * @param pageSize
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link MessageEndPointTypes.MessageListResponseDto}
     */
    @GetMapping(value = "/v1/messages/helpdesk")
    ResponseEntity<RestResponse<PageResultDto<ServerMessageDto>>> getHelpdeskInbox(
        @RequestParam(name = "page",      required = false) Integer       pageIndex,
        @RequestParam(name = "size",      required = false) Integer       pageSize,
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @RequestParam(name = "date-to",   required = false) ZonedDateTime dateTo,
        @RequestParam(name = "read",      required = false) Boolean       read
    );

    /**
     * Count helpdesk unassigned messages
     *
     * @return The number of unassigned messages
     */
    @GetMapping(value = "/v1/messages/helpdesk/count")
    ResponseEntity<RestResponse<Long>> countUnassignedMessages();

    /**
     * Find user messages
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link MessageEndPointTypes.MessageListResponseDto}
     */
    @GetMapping(value = "/v1/messages/user/{userKey}")
    ResponseEntity<RestResponse<PageResultDto<ServerMessageDto>>> findMessages(
        @PathVariable(name = "userKey")                     UUID          userKey,
        @RequestParam(name = "page",      required = false) Integer       pageIndex,
        @RequestParam(name = "size",      required = false) Integer       pageSize,
        @RequestParam(name = "date-from", required = false) ZonedDateTime dateFrom,
        @RequestParam(name = "date-to",   required = false) ZonedDateTime dateTo,
        @RequestParam(name = "read",      required = false) Boolean       read
    );

    /**
     * Count user new (unread) messages
     *
     * @return The number of new messages
     */
    @GetMapping(value = "/v1/messages/user/{userKey}/count")
    ResponseEntity<RestResponse<Long>> countUserNewMessages(@PathVariable(name = "userKey") UUID userKey);

    /**
     * Send message
     *
     * @param userKey Recipient user unique key
     * @param message Message configuration object
     *
     * @return An instance of {@link BaseResponse}
     */
    @PostMapping(value = "/v1/messages")
    ResponseEntity<RestResponse<ServerMessageDto>> sendMessage(
        @RequestBody(required = true) ServerMessageCommandDto command
    );

    /**
     * Mark message as read
     *
     * @param key The message to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/messages/user/{owner}/message/{key}")
    ResponseEntity<RestResponse<ServerMessageDto>> readMessage(
        @PathVariable(name = "owner", required = true) UUID owner,
        @PathVariable(name = "key", required = true) UUID key
    );

    /**
     * Assign message to Helpdesk account
     *
     * @param messageKey The message unique key
     * @param recipientKey The recipient account unique key
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/messages/{messageKey}/recipient/{recipientKey}")
    ResponseEntity<RestResponse<ServerMessageDto>> assignMessage(
        @PathVariable(name = "messageKey", required = true) UUID messageKey,
        @PathVariable(name = "recipientKey", required = true) UUID recipientKey
    );

    /**
     * Get a message thread
     *
     * @param userKey The owner of the message
     * @param messageKey The key of any message from the thread
     *
     * @return An instance of {@link BaseResponse}
     */
    @GetMapping(value = "/v1/messages/thread/{threadKey}/sender/{ownerKey}")
    ResponseEntity<RestResponse<List<ServerMessageDto>>> getMessageThread(
        @PathVariable(name = "threadKey", required = true) UUID threadKey,
        @PathVariable(name = "ownerKey", required = true) UUID ownerKey
    );

    /**
     * Find notifications
     *
     * @param pageIndex
     * @param pageSize
     * @param userKey
     * @param dateFrom
     * @param dateTo
     * @param read
     *
     * @return An instance of {@link BaseResponse}
     */
    @GetMapping(value = "/v1/notifications")
    ResponseEntity<RestResponse<PageResultDto<ServerNotificationDto>>> findNotifications(
        @RequestParam(name = "page",      required = false) Integer                     pageIndex,
        @RequestParam(name = "size",      required = false) Integer                     pageSize,
        @RequestParam(name = "user",      required = true)  UUID                        userKey,
        @RequestParam(name = "date-from", required = false) ZonedDateTime               dateFrom,
        @RequestParam(name = "date-to",   required = false) ZonedDateTime               dateTo,
        @RequestParam(name = "read",      required = false) Boolean                     read,
        @RequestParam(name = "orderBy",   required = false) EnumNotificationSortField   orderBy,
        @RequestParam(name = "order",     required = false) EnumSortingOrder            order

    );

    /**
     * Send notification
     *
     * @param notification Notification command object
     *
     * @return An instance of {@link BaseResponse}
     */
    @PostMapping(value = "/v1/notifications")
    ResponseEntity<RestResponse<ServerNotificationDto>> sendNotification(
        @RequestBody(required = true) ServerNotificationCommandDto notification
    );

    /**
     * Mark notification as read
     *
     * @param key The key of the notification to mark as read
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/notifications/{key}")
    ResponseEntity<BaseResponse> readNotification(@PathVariable(name = "key", required = true) UUID key);

    /**
     * Mark all notifications as read
     *
     * @param key The key of the recipient
     *
     * @return An instance of {@link BaseResponse}
     */
    @PutMapping(value = "/v1/notifications/user/{key}")
    ResponseEntity<BaseResponse> readAllNotifications(@PathVariable(name = "key", required = true) UUID key);

}
