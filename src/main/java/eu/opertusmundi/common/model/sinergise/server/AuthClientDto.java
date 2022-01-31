package eu.opertusmundi.common.model.sinergise.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthClientDto {

    private String name;

    private String secret;

    private String type;

    private String redirectUrl;

}
