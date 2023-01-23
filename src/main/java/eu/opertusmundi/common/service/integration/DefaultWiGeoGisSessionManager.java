package eu.opertusmundi.common.service.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.integration.WiGeoGisFeignClient;
import eu.opertusmundi.common.repository.AccountCredentialsRepository;
import eu.opertusmundi.common.util.UrlUtils;

@ConditionalOnProperty(name = "opertusmundi.feign.wigeogis.url", matchIfMissing = true)
@Service
public class DefaultWiGeoGisSessionManager implements WiGeoGisSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWiGeoGisSessionManager.class);

    private final static String APPLICATION_NAME = "WiGeoGIS";

    @Value("${opertusmundi.feign.wigeogis.url:}")
    private String baseUrl;

    private final AccountCredentialsRepository        credentialsRepository;
    private final ObjectProvider<WiGeoGisFeignClient> client;

    @Autowired
    public DefaultWiGeoGisSessionManager(
        AccountCredentialsRepository        credentialsRepository,
        ObjectProvider<WiGeoGisFeignClient> client
    ) {
        this.credentialsRepository = credentialsRepository;
        this.client                = client;
    }

    @Override
    public String login(int topioAccountId) {
        try {
            final var credentials = this.credentialsRepository
                .findOneAccountByIdAndApplication(topioAccountId, APPLICATION_NAME)
                .orElse(null);
            if (credentials == null) {
                logger.warn("Credentials not found [account={}, application={}]", topioAccountId, APPLICATION_NAME);
                return null;
            }
            final var response    = this.client.getObject().login(credentials.getUsername(), credentials.getPassword(), 1);
            final var result      = response.getBody();
            final var link        = UrlUtils.appendPath(baseUrl, "/redirect/main/?token=");

            return result != null && result.isAuthenticated() ? link + result.getSession() : null;
        } catch (final Exception ex) {
            logger.error("[WiGeoGIS] Login failed", ex);
        }

        return null;
    }

}
