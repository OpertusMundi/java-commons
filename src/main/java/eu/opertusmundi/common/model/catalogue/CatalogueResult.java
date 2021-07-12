package eu.opertusmundi.common.model.catalogue;

import java.util.Collections;
import java.util.List;

import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CatalogueResult<T extends CatalogueItemDto> {

    private PageResultDto<T> result;

    private List<ProviderDto> publishers;

    public static <T extends CatalogueItemDto> CatalogueResult<T> empty(PageRequestDto pageRequest) {
        return new CatalogueResult<T>(PageResultDto.empty(pageRequest), Collections.emptyList());
    }
}
