package eu.opertusmundi.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.DiscoveryServiceFeignClient;
import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;
import eu.opertusmundi.common.model.discovery.client.DiscoveryServiceException;
import eu.opertusmundi.common.model.discovery.client.DiscoveryServiceMessageCode;
import feign.FeignException;

@ConditionalOnProperty(name = "opertusmundi.feign.discovery.url", matchIfMissing = true)
@Service
public class DefaultDiscoveryService implements DiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDiscoveryService.class);

    @Autowired
    private ObjectProvider<DiscoveryServiceFeignClient> client;

    @Override
    public ClientJoinableResultDto findJoinable(String id) {
        try {
            final var response = this.client.getObject().getJoinable(id);
            final var result   = response.getBody();

            return ClientJoinableResultDto.from(result);
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new DiscoveryServiceException(DiscoveryServiceMessageCode.DISCOVERY_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw DiscoveryServiceException.wrap(ex);
        }
    }

    @Override
    public ClientRelatedResultDto findRelated(String source, String[] target) {
        try {
            final var response = this.client.getObject().getRelated(source, target);
            final var result   = response.getBody();

            return ClientRelatedResultDto.from(result);
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new DiscoveryServiceException(DiscoveryServiceMessageCode.DISCOVERY_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw DiscoveryServiceException.wrap(ex);
        }
    }

}
