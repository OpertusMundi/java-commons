package eu.opertusmundi.common.feign.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
     * @param baseMapProvider
     * @param baseMapName
     * @param aspectRatio
     * @param height
     * @param width
     * @param lat
     * @param lon
     * @param time
     * @param crs
     * @param geometry
     * @return
     */
    @PostMapping(
        value   = "/profile/path/netcdf",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<DataProfilerDeferredResponseDto> profileNetCdf(
        @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
        @RequestPart(name = "resource",         required = true)  String     resource,
        @RequestPart(name = "response",         required = true)  String     response,
        @RequestPart(name = "basemap_provider", required = false) String     baseMapProvider,
        @RequestPart(name = "basemap_name ",    required = false) String     baseMapName ,
        @RequestPart(name = "aspect_ratio",     required = false) BigDecimal aspectRatio,
        @RequestPart(name = "height",           required = false) Integer    height,
        @RequestPart(name = "width",            required = false) Integer    width,
        @RequestPart(name = "lat",              required = false) String     lat ,
        @RequestPart(name = "lon",              required = false) String     lon ,
        @RequestPart(name = "time",             required = false) String     time,
        @RequestPart(name = "crs",              required = false) String     crs,
        @RequestPart(name = "geometry",         required = false) String     geometry 
    );

    /**
     * Start new job for raster data
     *
     * @param resource
     * @param response
     * @return
     */
    @PostMapping(
        value   = "/profile/path/raster",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<DataProfilerDeferredResponseDto> profileRaster(
        @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
        @RequestPart(name = "resource", required = true) String resource,
        @RequestPart(name = "response", required = true) String response
    );

    /**
     * Start new job for vector data
     * 
     * @param resource
     * @param response
     * @param baseMapProvider
     * @param baseMapName
     * @param aspectRatio
     * @param height
     * @param width
     * @param lat
     * @param lon
     * @param time
     * @param crs
     * @param geometry
     * @return
     */
    @PostMapping(
        value   = "/profile/path/vector",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ResponseEntity<DataProfilerDeferredResponseDto> profileVector(
        @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
        @RequestPart(name = "resource",         required = true) String      resource,
        @RequestPart(name = "response",         required = true)  String     response,
        @RequestPart(name = "basemap_provider", required = false) String     baseMapProvider,
        @RequestPart(name = "basemap_name ",    required = false) String     baseMapName ,
        @RequestPart(name = "aspect_ratio",     required = false) BigDecimal aspectRatio,
        @RequestPart(name = "height",           required = false) Integer    height,
        @RequestPart(name = "width",            required = false) Integer    width,
        @RequestPart(name = "lat",              required = false) String     lat ,
        @RequestPart(name = "lon",              required = false) String     lon ,
        @RequestPart(name = "time",             required = false) String     time,
        @RequestPart(name = "crs",              required = false) String     crs,
        @RequestPart(name = "geometry",         required = false) String     geometry 
    );

    /**
     * Get ticket status
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/status/{ticket}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DataProfilerStatusResponseDto> getStatus(@PathVariable("ticket") String ticket);

    /**
     * Get resource metadata
     *
     * @param ticket Ticket unique id
     * @return
     */
    @GetMapping(value = "/resource/{ticket}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> getMetadata(@PathVariable("ticket") String ticket);

}
