package eu.opertusmundi.common.feign.client;

import java.io.File;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.feign.client.config.DataProfilerServiceClientConfiguration;
import eu.opertusmundi.common.model.profiler.DataProfilerDeferredResponseDto;
import eu.opertusmundi.common.model.profiler.DataProfilerStatusResponseDto;
import feign.Headers;

@FeignClient(
    name = "${opertusmundi.feign.data-profiler.name}",
    url = "${opertusmundi.feign.data-profiler.url}",
    configuration = DataProfilerServiceClientConfiguration.class
)
public interface DataProfilerServiceFeignClient {

    /**
     * Start new job for data in NetCDF format
     *
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/profile/file/netcdf",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<DataProfilerDeferredResponseDto> profileNetCdf(
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String response
    );

    /**
     * Start new job for raster data
     *
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/profile/file/raster",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<DataProfilerDeferredResponseDto> profileRaster(
        @RequestPart(name = "resource", required = true) File resource,
        @RequestPart(name = "response", required = true) String response
    );

    /**
     * Start new job for vector data
     *
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/profile/file/vector",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<DataProfilerDeferredResponseDto> profileVector(
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
    ResponseEntity<DataProfilerStatusResponseDto> getStatus(@RequestParam("ticket") String ticket);

    /**
     * Get resource metadata
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/resource/{ticket}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> getMetadata(@RequestParam("ticket") String ticket);

}
