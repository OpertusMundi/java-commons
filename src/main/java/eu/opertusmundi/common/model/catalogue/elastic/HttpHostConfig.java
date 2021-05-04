package eu.opertusmundi.common.model.catalogue.elastic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpHostConfig {

    /**
     * The hostname (IP or DNS name)
     */
    private String hostName;
    
    /**
     * The port number.
     * {@code -1} indicates the scheme default port.
     */
    private int port;
    
    /**
     * The name of the scheme.
     * {@code null} indicates the default {@code HTTP} scheme
     */
    private String scheme;
    
}
