package eu.opertusmundi.common.model.integration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WiGeoGisLoginResultDto {

    private boolean authenticated;
    private String  session;
    private String  status;

}
