package eu.opertusmundi.common.model.catalogue.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.util.StreamUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueClientCollectionResponse<T> extends RestResponse<PageResultDto<T>> {

    private CatalogueClientCollectionResponse(PageResultDto<T> page, List<ProviderDto> publishers) {
        super(page);

        this.publishers = new HashMap<UUID, ProviderDto>();

        StreamUtils.from(publishers).forEach(p -> {
            if (!this.publishers.containsKey(p.getKey())) {
                this.publishers.put(p.getKey(), p);
            }
        });
    }

    public static <T> CatalogueClientCollectionResponse<T> of(PageResultDto<T> page, List<ProviderDto> publishers) {
        return new CatalogueClientCollectionResponse<>(page, publishers);
    }

    @Schema(description = "Map with all publishers for all catalogue items in the response. The key is the publisher id.")
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, ProviderDto> publishers;

}
