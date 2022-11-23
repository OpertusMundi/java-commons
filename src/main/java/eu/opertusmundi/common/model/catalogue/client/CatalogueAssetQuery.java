package eu.opertusmundi.common.model.catalogue.client;

import java.util.Set;

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
import lombok.ToString;

@Schema(description = "Catalogue query")
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class CatalogueAssetQuery extends PageRequestDto {

    @Builder
    public CatalogueAssetQuery(
        int page, int size, String publisherKey, Set<EnumAssetType> type, String query, EnumCatalogueSortField orderBy, EnumSortingOrder order
    ) {
        super(page, size);

        this.order        = order;
        this.orderBy      = orderBy;
        this.publisherKey = publisherKey;
        this.query        = query;
        this.type         = type;
    }

    @JsonIgnore
    @Hidden
    private String publisherKey;

    @Schema(description = "Query string used for full text search operation")
    private String query;

    @Schema(description = "Filter assets by type")
    private Set<EnumAssetType> type;

    @Schema(description = "Sorting order", defaultValue = "ASC")
    private EnumSortingOrder order;

    @Schema(description = "Order by property")
    private EnumCatalogueSortField orderBy;

    public PageRequestDto toPageRequest() {
        return PageRequestDto.of(this.page, this.size);
    }

    public CatalogueAssetQuery next() {
        return new CatalogueAssetQuery(this.page + 1, this.size, publisherKey, type, query, orderBy, order);
    }

}
