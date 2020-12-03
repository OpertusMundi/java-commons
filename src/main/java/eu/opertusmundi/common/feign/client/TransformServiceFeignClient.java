package eu.opertusmundi.common.feign.client;

import java.io.File;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import eu.opertusmundi.common.feign.client.config.TransformServiceClientConfiguration;
import eu.opertusmundi.common.model.transform.ServerStatusResponseDto;
import eu.opertusmundi.common.model.transform.ServerTransformDeferredResponseDto;
import feign.Headers;
import feign.Response;

@FeignClient(
    name = "${opertusmundi.feign.transform.name}",
    url = "${opertusmundi.feign.transform.url}",
    configuration = TransformServiceClientConfiguration.class
)
public interface TransformServiceFeignClient {

    /**
     * Start new synchronous job
     *
     * @param sourceType
     * @param format
     * @param sourceCrs
     * @param targetCrs
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/transform",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    Response transformSync(
        @RequestPart(name = "src_type", required = true) String sourceType,
        @RequestPart(name = "format", required = true) String format,
        @RequestPart(name = "from", required = true) String sourceCrs,
        @RequestPart(name = "to", required = true) String targetCrs,
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String response
    );

    /**
     * Start new asynchronous job
     *
     * @param sourceType
     * @param format
     * @param sourceCrs
     * @param targetCrs
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/transform",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<ServerTransformDeferredResponseDto> transformAsync(
        @RequestPart(name = "src_type", required = true) String sourceType,
        @RequestPart(name = "format", required = true) String format,
        @RequestPart(name = "from", required = false) String sourceCrs,
        @RequestPart(name = "to", required = true) String targetCrs,
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String response
    );

    /**
     * Get ticket status
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/status/{ticket}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ServerStatusResponseDto> getStatus(@RequestParam("ticket") String ticket);

    /**
     * Get resource
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/resource/{ticket}", produces = MediaType.APPLICATION_JSON_VALUE)
    Response getResource(@RequestParam("ticket") String ticket);

}
