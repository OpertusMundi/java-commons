package eu.opertusmundi.common.model.catalogue.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.catalogue.EnumCatalogueSortField;
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
    public CatalogueAssetQuery(
        int page, int size, String publisherKey, String query, EnumCatalogueSortField orderBy, EnumSortingOrder order
    ) {
        super(page, size);

        this.order        = order;
        this.orderBy      = orderBy;
        this.publisherKey = publisherKey;
        this.query        = query;
    }

    @JsonIgnore
    @Hidden
    private String publisherKey;

    @Schema(description = "Query string used for full text search operation")
    private String query;

    @Schema(description = "Sorting order", defaultValue = "ASC")
    private EnumSortingOrder order;

    @Schema(description = "Order by property")
    private EnumCatalogueSortField orderBy;

    public PageRequestDto toPageRequest() {
        return PageRequestDto.of(this.page, this.size);
    }

    public CatalogueAssetQuery next() {
        return new CatalogueAssetQuery(this.page + 1, this.size, publisherKey, query, orderBy, order);
    }

    @Override
    public String toString() {
        return String.format(
            "Catalogue asset query [page: %d, size %d, publisherKey: %s, query: %s, order: %s, orderBy: %s]",
            page, size, publisherKey, query, order, orderBy
        );
    }

}
