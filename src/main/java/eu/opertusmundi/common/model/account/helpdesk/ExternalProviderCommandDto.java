package eu.opertusmundi.common.model.account.helpdesk;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.integration.EnumDataProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalProviderCommandDto {

    @JsonIgnore
    private int userId;

    @JsonIgnore
    private UUID customerKey;

    private EnumDataProvider provider;
}
