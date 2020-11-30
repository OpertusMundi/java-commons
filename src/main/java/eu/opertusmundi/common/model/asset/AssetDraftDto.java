package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.dto.PublisherDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftDto extends BaseAssetDraftDto {

    private PublisherDto publisher;

}
