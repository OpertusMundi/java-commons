package eu.opertusmundi.common.feign.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.feign.client.config.IngestServiceClientConfiguration;
import eu.opertusmundi.common.model.ipr.ServerEmbedFictitiousEntriesCommandDto;
import eu.opertusmundi.common.model.ipr.ServerIprDeferredResponseDto;
import eu.opertusmundi.common.model.ipr.ServerIprJobStatusResponseDto;

@ConditionalOnProperty(name = "opertusmundi.feign.ipr.name", matchIfMissing = true)
@FeignClient(
    name = "ipr-service",
    url = "${opertusmundi.feign.ipr.url}",
    configuration = IngestServiceClientConfiguration.class
)
public interface IprServiceFeignClient {

    /**
     * Embed fictitious entries in the dataset, according to the given unique
     * key
     *
     * @param idempotencyKey
     * @param command
     * @return
     */
    @PostMapping(
        value   = "/vector/embed/fictitious",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ServerIprDeferredResponseDto> embedFictitious(
        @RequestHeader("X-Idempotence-Key") String idempotencyKey,
        @RequestBody ServerEmbedFictitiousEntriesCommandDto command

    );

    /**
     * Embed collinear points in selected geometries of the dataset, according
     * to the given unique key
     *
     * @param idempotencyKey
     * @param command
     * @return
     */
    @PostMapping(
        value   = "/vector/embed/geometries",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ServerIprDeferredResponseDto> embedGeometries(
        @RequestHeader("X-Idempotence-Key") String idempotencyKey,
        @RequestBody ServerEmbedFictitiousEntriesCommandDto command

    );

    /**
     * Returns the status of the process identified by a ticket or idempotency key
     *
     * @param ticket
     * @param idempotencyKey
     * @return
     */
    @GetMapping(value = "/jobs/status", produces = "application/json")
    ResponseEntity<ServerIprJobStatusResponseDto> getStatus(
        @RequestParam(name = "ticket", required = false) String ticket,
        @RequestParam(name = "idempotency-key", required = false) String idempotencyKey
    );

}
