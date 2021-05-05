package eu.opertusmundi.common.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.service.ElasticSearchService;

@ConditionalOnProperty(name = "opertusmundi.elastic.migrate-on-startup")
@Component
public class ElasticApplicationRunner implements ApplicationRunner {

    private static final Logger logger = LogManager.getLogger(ElasticApplicationRunner.class);

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            this.initializeIndices();
        } catch (final Exception ex) {
            logger.error("Failed to initialize Elastic Search indices", ex);

            throw ex;
        }
    }

    private void initializeIndices() throws ElasticServiceException {
        if (this.elasticSearchService != null) {
            this.elasticSearchService.initializeIndices();
        }
    }

}
