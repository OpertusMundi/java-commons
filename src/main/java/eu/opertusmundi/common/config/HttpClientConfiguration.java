package eu.opertusmundi.common.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientConfiguration.class);

    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    @Value("${http-client.maxTotal:200}")
    private int maxTotal;

    @Value("${http-client.maxPerRoute:20}")
    private int maxPerRoute;

    @Value("${http-client.ingore-ssl-validation:false}")
    private boolean ignoreCertificateValidation;

    @PostConstruct
    public void init() throws Exception {
        try {
            if(this.ignoreCertificateValidation) {
                logger.warn("Using non-validating SSL HttpClient");

                final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();

                this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                        .build()
                );
            } else {
                this.poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
            }

            this.poolingHttpClientConnectionManager.setMaxTotal(this.maxTotal);
            this.poolingHttpClientConnectionManager.setDefaultMaxPerRoute(this.maxPerRoute);
        } catch (final Exception ex) {
            logger.error("Failed to create HttpClientConnectionManager", ex);
            throw ex;
        }
    }

    @PreDestroy
    public void destroy() {
        this.poolingHttpClientConnectionManager.shutdown();
    }

    @Bean
    public CloseableHttpClient defaultHttpClient() {
        return HttpClients
            .custom()
            .setConnectionManager(this.poolingHttpClientConnectionManager)
            .build();
    }

}
