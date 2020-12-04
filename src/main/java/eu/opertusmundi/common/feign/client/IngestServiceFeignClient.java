package eu.opertusmundi.common.feign.client;

import java.io.File;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import eu.opertusmundi.common.feign.client.config.IngestServiceClientConfiguration;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestEndpointsResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
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
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<ServerIngestEndpointsResponseDto> ingestSync(
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String responseType
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
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<ServerIngestDeferredResponseDto> ingestAsync(
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String responseType
    );

    /**
     * Get ticket status
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/status/{ticket}", produces = "application/json")
    ResponseEntity<ServerIngestStatusResponseDto> getStatus(@RequestParam("ticket") String ticket);

    /**
     * Get endpoints for ticket
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/endpoints/{ticket}", produces = "application/json")
    ResponseEntity<ServerIngestEndpointsResponseDto> getEndpoints(@RequestParam("ticket") String ticket);

}
