package eu.opertusmundi.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(prefix = "opertusmundi.contract.signpdf", name = "key-store")
public class KeystoreConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(KeystoreConfiguration.class);
    
    private Resource keystoreResource;
   
    private String keystorePassword;
    
    private KeyStore keystore;
    
    @Autowired
    void setKeystoreResource(
        @Value("${opertusmundi.contract.signpdf.key-store}") Resource keystoreResource)
    {
        Assert.notNull(keystoreResource, "keystoreResource");
        this.keystoreResource = keystoreResource;
    }
    
    @Autowired
    void setKeystorePassword(
        @Value("${opertusmundi.contract.signpdf.key-store-password}") String keystorePassword) 
    {
        Assert.isTrue(!StringUtils.isEmpty(keystorePassword), "keystorePassword cannot be empty");
        this.keystorePassword = keystorePassword;
    }
    
    private KeyStore keystoreFromFile()
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
    {
        final File keystoreFile = keystoreResource.getFile();
        final KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(keystoreFile)) {
            keystore.load(in, keystorePassword.toCharArray());
        }
        
        logger.info("Loaded keystore from {} ({} entries)", keystoreFile, keystore.size());
        return keystore;
    }
    
    @PostConstruct
    void initialize() 
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
    {
        this.keystore = keystoreFromFile();
    }
    
    @Bean("signatoryKeyStore")
    public KeyStore signatoryKeyStore()
    {
        return keystore;
    }
}
