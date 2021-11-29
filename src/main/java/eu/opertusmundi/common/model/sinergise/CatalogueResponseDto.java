package eu.opertusmundi.common.model.sinergise;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogueResponseDto {

    private List<FeatureDto> features;
    private List<LinkDto>    links;
    private ContextDto       context;
}
