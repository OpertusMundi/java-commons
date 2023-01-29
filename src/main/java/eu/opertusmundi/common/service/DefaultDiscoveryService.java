package eu.opertusmundi.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.DiscoveryServiceFeignClient;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;

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
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public ClientRelatedResultDto findRelated(String source, String[] target) {
        try {
            final var response = this.client.getObject().getRelated(source, target);
            final var result   = response.getBody();

            return ClientRelatedResultDto.from(result);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

}
