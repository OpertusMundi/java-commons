package eu.opertusmundi.common.model.catalogue.server;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
@Builder
public class CatalogueAdditionalResource {

    private String                      id;
    private EnumAssetAdditionalResource type;
    private String                      name;
    private String                      value;

    @JsonInclude(Include.NON_NULL)
    private Long                        size;

    @JsonProperty("modified_on")
    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime modifiedOn;

}
