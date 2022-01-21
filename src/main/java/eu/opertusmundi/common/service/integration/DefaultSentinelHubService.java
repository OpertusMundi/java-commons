package eu.opertusmundi.common.service.integration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.config.SentinelHubConfiguration;
import eu.opertusmundi.common.feign.client.SentinelHubFeignClient;
import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto.Billing;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubException;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubExceptionMessageCode;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubSecurityException;
import eu.opertusmundi.common.model.sinergise.server.ServerCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.server.ServerTokenResponseDto;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.codec.Decoder;
import feign.codec.Encoder;

@Service
@ConditionalOnProperty(name = "opertusmundi.sentinel-hub.enabled")
@Import(FeignClientsConfiguration.class)
public class DefaultSentinelHubService implements SentinelHubService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSentinelHubService.class);

    @Value("${opertusmundi.sentinel-hub.oauth-client-name}")
    private String oauthClientName;

    @Value("${opertusmundi.sentinel-hub.client-id}")
    private String clientId;

    @Value("${opertusmundi.sentinel-hub.client-secret}")
    private String clientSecret;

    @Autowired
    private SentinelHubConfiguration config;

    private final Map<String, SentinelHubFeignClient> clients = new HashMap<>();

    private String authorizationHeader;

    @Autowired
    public void setupClients(
        Encoder encoder, Decoder decoder, Contract contract
    ) {
        config.getDeployments().stream().forEach(d-> {
            final SentinelHubFeignClient c = Feign.builder()
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .target(SentinelHubFeignClient.class, d.getUrl());

            this.clients.put(d.getName(), c);
        });
    }

    @Override
    public String requestToken() throws SentinelHubException {
        try {
            // Prevent concurrent token requests
            synchronized (this) {
                final SentinelHubFeignClient client   = this.clients.get(this.oauthClientName);
                final ServerTokenResponseDto response = client.requestToken("client_credentials", clientId, clientSecret);

                if (response.isSuccess()) {
                    this.authorizationHeader = String.format("Bearer %s", response.getAccessToken());
                    return response.getAccessToken();
                }

                throw new SentinelHubSecurityException(response.getError().getMessage());
            }
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
        } catch (final SentinelHubException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw SentinelHubException.wrap(ex);
        }
    }

    @Override
    public CatalogueResponseDto search(ClientCatalogueQueryDto query) throws SentinelHubException {
        return this.search(query, true);
    }

    private CatalogueResponseDto search(ClientCatalogueQueryDto query, boolean requestTokenOnFail) throws SentinelHubException {
        try {
            // Initialize token on first request
            if (StringUtils.isEmpty(authorizationHeader)) {
                // Do not retry search operation, if token request operation
                // fails
                requestTokenOnFail = false;

                this.requestToken();
            }
            final ServerCatalogueQueryDto serverQuery = ServerCatalogueQueryDto.builder()
                .bbox(query.getBbox())
                .collections(new String[]{query.getCollection()})
                .datetime(query.getDateTime())
                .distinct(query.getDistinct())
                .fields(query.getFields())
                .ids(query.getIds())
                .intersects(query.getIntersects())
                .limit(query.getLimit())
                .next(query.getNext())
                .query(query.getQuery())
                .build();

            final SentinelHubConfiguration.Deployment deployment = this.config.getDeploymentByCollection(query.getCollection());
            final SentinelHubFeignClient              client     = this.clients.get(deployment.getName());
            final CatalogueResponseDto                response   = client.search(authorizationHeader, serverQuery);

            return response;
        } catch (final FeignException fex) {
            // If 401 status code is returned, request a new token and retry
            if (fex.status() == HttpStatus.UNAUTHORIZED.value() && requestTokenOnFail) {
                this.requestToken();
                return this.search(query, false);
            }

            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw SentinelHubException.wrap(ex);
        }
    }

    @Override
    @Cacheable(
        cacheNames = "sentinel-hub-subscription-plans",
        cacheManager = "defaultCacheManager",
        key = "'sentinel-hub-subscription-plans'"
    )
    public List<SubscriptionPlanDto> getSubscriptionPlans() {
        final SubscriptionPlanDto plan = SubscriptionPlanDto.builder()
            .id(UUID.randomUUID().toString())
            .title("Exploration")
            .billing(Billing.builder()
                .annually(BigDecimal.valueOf(300))
                .monthly(BigDecimal.valueOf(25))
                .build()
            )
            .description("Non-Commercial / Research")
            .features(Arrays.<String[]>asList(
                new String[] {"All free features"},
                new String[] {"OGC standard WMS / WCS / WMTS / WFS", "API for advanced features","Configuration Utility tool"}
            ))
            .license("Creative Commons Attribution-NonCommercial 4.0 International License")
            .build();

        final List<SubscriptionPlanDto> result = Arrays.asList(plan);

        return result;
    }

}
