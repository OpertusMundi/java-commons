package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.catalogue.server.CatalogueAdditionalResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetUriAdditionalResourceDto extends AssetAdditionalResourceDto {

    private static final long serialVersionUID = 1L;

    public AssetUriAdditionalResourceDto() {
        super(EnumAssetAdditionalResource.URI);
    }

    public AssetUriAdditionalResourceDto(String uri, String text) {
        this();

        this.uri  = uri;
        this.text = text;
    }

    @Schema(description = "The text displayed for the URI. If not set, the uri value is used as text")
    private String text;

    @Schema(description = "The URI value")
    private String uri;

    @Override
    public CatalogueAdditionalResource toCatalogueResource() {
        return CatalogueAdditionalResource.builder()
            .id("")
            .name(this.text)
            .value(this.uri)
            .type(this.type)
            .build();
    }

}
