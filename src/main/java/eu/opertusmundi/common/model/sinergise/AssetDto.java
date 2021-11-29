package eu.opertusmundi.common.model.sinergise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDto {

    private String   href;
    private String   title;
    private String   description;
    private String   type;
    private String[] roles;

}
