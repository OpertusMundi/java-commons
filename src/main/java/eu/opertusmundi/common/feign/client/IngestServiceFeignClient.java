package eu.opertusmundi.common.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import eu.opertusmundi.common.feign.client.config.IngestServiceClientConfiguration;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
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
     * Start a new asynchronous job
     *
     * @param idempotencyKey
     * @param resource
     * @param responseType
     * @param table
     * @param workspace
     * @param shard
     * @param replace
     * @param encoding
     * @param crs
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
        @RequestPart(name = "resource",  required = true)  String resource,
        @RequestPart(name = "response",  required = true)  String responseType,
        @RequestPart(name = "table",     required = true)  String table,
        @RequestPart(name = "workspace", required = true)  String workspace,
        @RequestPart(name = "shard",     required = false) String shard,
        @RequestPart(name = "replace",   required = false) boolean replace,
        @RequestPart(name = "encoding",  required = false) String encoding,
        @RequestPart(name = "crs",       required = false) String crs

    );

    /**
     * Publish a layer to a Geoserver instance
     *
     * @param idempotencyKey
     * @param command
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
    ResponseEntity<ServerIngestStatusResponseDto> getTicketStatus(@PathVariable String ticket);

    /**
     * Get result for ticket
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/result/{ticket}", produces = "application/json")
    ResponseEntity<ServerIngestResultResponseDto> getTicketResult(@PathVariable String ticket);

    /**
     * Get ticket from idempotent key
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/ticket_by_key/{key}", produces = "application/json")
    ResponseEntity<ServerIngestTicketResponseDto> getTicketFromIdempotentKey(@PathVariable String key);

    /**
     * Drop PostGis table created from a previous ingest operation
     *
     * @param table Database table name
     * @param workspace The workspace that the layer belongs; if not present, the default workspace will be assumed
     * @param shard The Geoserver shard
     * @return
     */
    @DeleteMapping(value = "/ingest")
    ResponseEntity<Void> removeTable(
        @RequestParam(required = true)  String table,
        @RequestParam(required = true)  String workspace,
        @RequestParam(required = false) String shard
    );

    /**
     * Remove both the layer and feature type from GeoServer.
     *
     * @param table Database table name
     * @param workspace The workspace that the layer belongs; if not present, the default workspace will be assumed
     * @param shard The Geoserver shard
     * @return
     */
    @DeleteMapping(value = "/publish")
    ResponseEntity<Void> removeLayer(
        @RequestParam(required = true)  String table,
        @RequestParam(required = true)  String workspace,
        @RequestParam(required = false) String shard
    );

}
