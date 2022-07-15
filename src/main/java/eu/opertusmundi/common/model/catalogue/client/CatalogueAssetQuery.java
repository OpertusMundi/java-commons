package eu.opertusmundi.common.model.catalogue.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.PageRequestDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Catalogue query")
@NoArgsConstructor
@Getter
@Setter
public class CatalogueAssetQuery extends PageRequestDto {

    @Builder
    public CatalogueAssetQuery(int page, int size, String publisherKey, String query) {
        super(page, size);

        this.publisherKey = publisherKey;
        this.query        = query;
    }

    @JsonIgnore
    @Hidden
    private String publisherKey;

    @Schema(description = "Query string used for full text search operation")
    private String query;

    public PageRequestDto toPageRequest() {
        return PageRequestDto.of(this.page, this.size);
    }

    public CatalogueAssetQuery next() {
        return new CatalogueAssetQuery(this.page + 1, this.size, publisherKey, query);
    }

    @Override
    public String toString() {
        return String.format("Catalogue asset query [page: %d, size %d, publisherKey: %s, query: %s]", page, size, publisherKey, query);
    }

}
