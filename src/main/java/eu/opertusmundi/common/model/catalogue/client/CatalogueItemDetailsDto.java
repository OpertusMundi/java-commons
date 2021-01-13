package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.dto.PublisherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class CatalogueItemDetailsDto extends CatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected CatalogueItemDetailsDto() {
        super();
    }

    public CatalogueItemDetailsDto(CatalogueFeature feature) {
        super(feature);
    }

    @Schema(description = "Publisher details")
    @JsonProperty(access = Access.READ_ONLY)
    @Getter
    private PublisherDto publisher;

    public void setPublisher(PublisherDto publisher) {
        Assert.isTrue(publisher.getKey().equals(this.publisherId), "Provider account key does not match publisher id");

        this.publisher = publisher;
    }

}
