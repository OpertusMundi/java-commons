package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDraftDto extends CatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemDraftDto(CatalogueFeature feature) {
        super(feature);

        this.status = EnumDraftStatus.fromValue(feature.getProperties().getStatus());
    }
   
    @Schema(description = "Publisher details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private ProviderDto publisher;

    public void setPublisher(ProviderDto publisher) {
        Assert.isTrue(publisher.getKey().equals(this.publisherId), "Provider account key does not match publisher id");

        this.publisher = publisher;
    }

    @Getter
    @Setter
    private EnumDraftStatus status;

}
