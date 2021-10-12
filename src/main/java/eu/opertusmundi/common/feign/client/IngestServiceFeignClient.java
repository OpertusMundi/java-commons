package eu.opertusmundi.common.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import eu.opertusmundi.common.feign.client.config.IngestServiceClientConfiguration;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPromptResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishCommandDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestTicketResponseDto;
import feign.Headers;

@FeignClient(
    name = "${opertusmundi.feign.ingest.name}",
    url = "${opertusmundi.feign.ingest.url}",
    configuration = IngestServiceClientConfiguration.class
)
public interface IngestServiceFeignClient {

    /**
     * Start a new synchronous job
     *
     * @param resource Resource file
     * @param responseType Response type
     * @return
     */
    @PostMapping(
        value   = "/ingest",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<ServerIngestPromptResponseDto> ingestSync(
        @RequestHeader("X-Idempotency-Key") String idempotencyKey,
        @RequestPart(name = "resource", required = true) String resource,
        @RequestPart(name = "response", required = true) String responseType,
        @RequestPart(name = "schema", required = false) String schema,
        @RequestPart(name = "tablename", required = false) String tablename
    );

    /**
     * Start a new asynchronous job
     *
     * @param resource Resource file
     * @param responseType Response type
     * @return
     */
    @PostMapping(
        value   = "/ingest",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<ServerIngestDeferredResponseDto> ingestAsync(
        @RequestHeader("X-Idempotency-Key") String idempotencyKey,
        @RequestPart(name = "resource", required = true) String resource,
        @RequestPart(name = "response", required = true) String responseType,
        @RequestPart(name = "schema", required = false) String schema,
        @RequestPart(name = "tablename", required = false) String tablename
    );

    /**
     * Start a new asynchronous job
     *
     * @param resource Resource file
     * @param responseType Response type
     * @return
     */
    @PostMapping(
        value   = "/publish",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<ServerIngestPublishResponseDto> publish(
        @RequestHeader("X-Idempotency-Key") String idempotencyKey, 
        @RequestBody ServerIngestPublishCommandDto command
    );
    
    /**
     * Get ticket status
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/status/{ticket}", produces = "application/json")
    ResponseEntity<ServerIngestStatusResponseDto> getTicketStatus(@PathVariable("ticket") String ticket);

    /**
     * Get result for ticket
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/result/{ticket}", produces = "application/json")
    ResponseEntity<ServerIngestResultResponseDto> getTicketResult(@PathVariable("ticket") String ticket);

    /**
     * Get ticket from idempotent key
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/ticket_by_key/{key}", produces = "application/json")
    ResponseEntity<ServerIngestTicketResponseDto> getTicketFromIdempotentKey(@PathVariable("key") String key);
    
}
