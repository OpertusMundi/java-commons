package eu.opertusmundi.common.service.integration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
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
import org.springframework.util.Assert;

import eu.opertusmundi.common.config.SentinelHubConfiguration;
import eu.opertusmundi.common.feign.client.SentinelHubFeignClient;
import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto.Billing;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto.ProcessingUnits;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto.Requests;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.client.SentinelHubDeployment;
import eu.opertusmundi.common.model.sinergise.client.SentinelHubOpenDataCollection;
import eu.opertusmundi.common.model.sinergise.server.AccountTypeDto;
import eu.opertusmundi.common.model.sinergise.server.ContractDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractCommandDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractResponse;
import eu.opertusmundi.common.model.sinergise.server.GroupDto;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubException;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubExceptionMessageCode;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubSecurityException;
import eu.opertusmundi.common.model.sinergise.server.ServerCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.server.ServerTokenResponseDto;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.httpclient.ApacheHttpClient;

@Service
@ConditionalOnProperty(name = "opertusmundi.sentinel-hub.enabled")
@Import(FeignClientsConfiguration.class)
public class DefaultSentinelHubService implements SentinelHubService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSentinelHubService.class);

    @Value("${opertusmundi.sentinel-hub.contracts.enabled:false}")
    private boolean contractsEnabled;

    @Value("${opertusmundi.sentinel-hub.default-client-name}")
    private String defaultClientName;

    @Value("${opertusmundi.sentinel-hub.client-id}")
    private String clientId;

    @Value("${opertusmundi.sentinel-hub.client-secret}")
    private String clientSecret;

    @Value("${feign.client.config.default.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${feign.client.config.default.readTimeout:60000}")
    private int readTimeout;

    @Autowired
    private SentinelHubConfiguration config;

    @Autowired
    private CloseableHttpClient httpClient;

    /**
     * Map of Feign clients for Sentinel Hub endpoints
     */
    private final Map<String, SentinelHubFeignClient> clients = new HashMap<>();

    /**
     * Current authorization header
     */
    private String authorizationHeader;

    /**
     * Sentinel Hub group identifier for Topio client
     */
    private UUID groupId;

    @Autowired
    public void setupClients(
        Encoder encoder, Decoder decoder, Contract contract
    ) {
        config.getDeployments().stream().forEach(d-> {
            final SentinelHubFeignClient c = Feign.builder()
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .client(new ApacheHttpClient(httpClient))
                .options(new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true))
                .target(SentinelHubFeignClient.class, d.getUrl());

            this.clients.put(d.getName(), c);
        });

        final List<GroupDto> groups = this.getGroups();

        if (contractsEnabled) {
            Assert.isTrue(groups.size() == 1, "Expected a single Sentinel Hub group");

            this.groupId = groups.get(0).getId();
        }
    }

    @Override
    public String requestToken() throws SentinelHubException {
        try {
            // Prevent concurrent token requests
            synchronized (this) {
                final SentinelHubFeignClient client   = this.clients.get(this.defaultClientName);
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
    public List<GroupDto> getGroups() {
        return this.getGroups(true);
    }

    private List<GroupDto> getGroups(boolean requestTokenOnFail) throws SentinelHubException {
        try {
            // Initialize token on first request
            if (StringUtils.isEmpty(authorizationHeader)) {
                // Do not retry, if token request operation fails
                requestTokenOnFail = false;

                this.requestToken();
            }

            final SentinelHubFeignClient client   = this.clients.get(this.defaultClientName);
            final List<GroupDto>         response = client.getGroups(this.authorizationHeader).getData();

            return response;
        } catch (final FeignException fex) {
            // If 401 status code is returned, request a new token and retry
            if (fex.status() == HttpStatus.UNAUTHORIZED.value() && requestTokenOnFail) {
                this.requestToken();
                return this.getGroups(false);
            }

            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw SentinelHubException.wrap(ex);
        }
    }

    @Override
    public List<AccountTypeDto> getAccountTypes() {
        return this.getAccountTypes(true);
    }

    private List<AccountTypeDto> getAccountTypes(boolean requestTokenOnFail) throws SentinelHubException {
        if (!this.contractsEnabled) {
            return Collections.emptyList();
        }

        try {
            // Initialize token on first request
            if (StringUtils.isEmpty(authorizationHeader)) {
                // Do not retry, if token request operation fails
                requestTokenOnFail = false;

                this.requestToken();
            }

            final SentinelHubFeignClient client   = this.clients.get(this.defaultClientName);
            final List<AccountTypeDto>   response = client.getAccountTypes(this.authorizationHeader, this.groupId).getData();

            return response;
        } catch (final FeignException fex) {
            // If 401 status code is returned, request a new token and retry
            if (fex.status() == HttpStatus.UNAUTHORIZED.value() && requestTokenOnFail) {
                this.requestToken();
                return this.getAccountTypes(false);
            }

            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw SentinelHubException.wrap(ex);
        }
    }

    @Override
    public List<ContractDto> getContracts() {
        return this.getContracts(true);
    }

    @Override
    public boolean contractExists(String userName) {
        final List<ContractDto> contracts = this.getContracts();

        final String text = "<" + userName + ">";

        return contracts.stream().anyMatch(c -> c.getName().contains(text));
    }

    private List<ContractDto> getContracts(boolean requestTokenOnFail) throws SentinelHubException {
        if (!this.contractsEnabled) {
            return Collections.emptyList();
        }

        try {
            // Initialize token on first request
            if (StringUtils.isEmpty(authorizationHeader)) {
                // Do not retry, if token request operation fails
                requestTokenOnFail = false;

                this.requestToken();
            }

            final SentinelHubFeignClient client   = this.clients.get(this.defaultClientName);
            final List<ContractDto>      response = client.getContracts(this.authorizationHeader, this.groupId).getData();

            return response;
        } catch (final FeignException fex) {
            // If 401 status code is returned, request a new token and retry
            if (fex.status() == HttpStatus.UNAUTHORIZED.value() && requestTokenOnFail) {
                this.requestToken();
                return this.getContracts(false);
            }

            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw SentinelHubException.wrap(ex);
        }
    }

    @Override
    public CreateContractResponse createContract(CreateContractCommandDto command) throws SentinelHubException {
        if (!this.contractsEnabled) {
            throw new SentinelHubException(SentinelHubExceptionMessageCode.VALIDATION, "Contracts are not supported");
        }

        return this.createContract(command, true);
    }

    private CreateContractResponse createContract(CreateContractCommandDto command, boolean requestTokenOnFail) throws SentinelHubException {
        try {
            // Initialize token on first request
            if (StringUtils.isEmpty(authorizationHeader)) {
                // Do not retry, if token request operation fails
                requestTokenOnFail = false;

                this.requestToken();
            }

            final SentinelHubFeignClient client   = this.clients.get(this.defaultClientName);
            final CreateContractResponse response = client.createContract(this.authorizationHeader, this.groupId, command);

            return response;
        } catch (final FeignException fex) {
            // If 401 status code is returned, request a new token and retry
            if (fex.status() == HttpStatus.UNAUTHORIZED.value() && requestTokenOnFail) {
                this.requestToken();
                return this.createContract(command, false);
            }

            logger.error("Operation has failed", fex);

            throw new SentinelHubException(SentinelHubExceptionMessageCode.CLIENT, fex.getMessage(), fex);
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

            final SentinelHubDeployment  deployment = this.config.getDeploymentByCollection(query.getCollection());
            final SentinelHubFeignClient client     = this.clients.get(deployment.getName());
            final CatalogueResponseDto   response   = client.search(authorizationHeader, serverQuery);

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
        if (!this.contractsEnabled) {
            return Collections.emptyList();
        }

        final List<AccountTypeDto> accountTypes = this.getAccountTypes();

        final List<SubscriptionPlanDto> result = accountTypes.stream()
            .filter(a -> !a.getName().equalsIgnoreCase("Trial"))
            .map(a -> {
                return SubscriptionPlanDto.builder()
                    .id(Long.toString(a.getId()))
                    .title(a.getName())
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
                    .processingUnits(ProcessingUnits.builder().minute(300L).month(30000L).build())
                    .requests(Requests.builder().minute(300L).month(100000L).build())
                    .license("Creative Commons Attribution-NonCommercial 4.0 International License")
                    .build();
            })
            .collect(Collectors.toList());

        return result;
    }


    @Override
    public List<SentinelHubOpenDataCollection> getOpenDataCollections() {
        return this.config.getDeployments().stream()
            .flatMap(d -> d.getCollections().stream())
            .collect(Collectors.toList());
    }

}
