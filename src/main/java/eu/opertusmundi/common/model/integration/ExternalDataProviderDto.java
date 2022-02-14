package eu.opertusmundi.common.model.integration;

import eu.opertusmundi.common.model.EnumRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
public class ExternalDataProviderDto {

    private EnumDataProvider id;

    private String name;

    private EnumRole requiredRole;

}
