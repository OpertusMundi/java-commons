package eu.opertusmundi.common.model.catalogue.integration;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommercialDataSentinelHubProperties extends SentinelHubProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    public CommercialDataSentinelHubProperties() {
        super(SentinelHubProperties.EnumType.COMMERCIAL);
    }

}
