package eu.opertusmundi.common.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.GeoIpApiFeignClient;
import eu.opertusmundi.common.model.location.GeoIpApiLocation;
import eu.opertusmundi.common.model.location.Location;
import feign.FeignException;

/**
 * Location service implementation using the IP Geolocation API
 *
 * @see https://ipgeolocationapi.com/
 */
@ConditionalOnProperty(name = "opertusmundi.ip-geolocation.enabled")
@Primary
@Service
public class GeoIpApiLocationService implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpApiLocationService.class);

    @Autowired
    private ObjectProvider<GeoIpApiFeignClient> client;

    @Override
    public Location getLocation(String ip) {
        try {
            if (StringUtils.isBlank(ip)) {
                return Location.empty(ip);
            }

            final ResponseEntity<GeoIpApiLocation> e = this.client.getObject().getLocation(ip);

            final GeoIpApiLocation result = e.getBody();

            if (result != null) {
                return result.toLocation(ip);
            }
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Geolocation API operation has failed. [ip={}, message={}]", ip, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("Geolocation API operation has failed. [ip={}, message={}]", ip, ex.getMessage());
        }

        logger.info("Geolocation API could not resolve location. [ip={}]", ip);

        return null;
    }

}
