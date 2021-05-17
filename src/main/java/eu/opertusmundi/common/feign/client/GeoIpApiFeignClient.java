package eu.opertusmundi.common.feign.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import eu.opertusmundi.common.model.location.GeoIpApiLocation;

@ConditionalOnProperty(name = "opertusmundi.ip-geolocation.enabled")
@FeignClient(
    name = "${opertusmundi.feign.ip-geolocation.geo-ip-api.name}",
    url = "${opertusmundi.feign.ip-geolocation.geo-ip-api.url}"
)
public interface GeoIpApiFeignClient {

    /**
     * Get location for remote IP address
     *
     * @param ip The client IP address
     *
     * @return An instance of {@link GeoIpApiLocation}
     */
    @GetMapping(value = "/{ip}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GeoIpApiLocation> getLocation(@PathVariable("ip") String query);


}
