package eu.opertusmundi.common.feign.client.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

import eu.opertusmundi.common.model.integration.WiGeoGisLoginResultDto;
import feign.Headers;

@ConditionalOnProperty(name = "opertusmundi.feign.wigeogis.url", matchIfMissing = true)
@FeignClient(name = "wigeogis", url = "${opertusmundi.feign.wigeogis.url}")
public interface WiGeoGisFeignClient {

    /**
     * Login
     *
     * @param username
     * @param password
     * @param duration
     * @return
     */
    @PostMapping(value = "/user/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: multipart/form-data")
    ResponseEntity<WiGeoGisLoginResultDto> login(
        @RequestPart("username") String username,
        @RequestPart("password") String password,
        @RequestPart("duration") int duration
    );

}
