package eu.opertusmundi.common.model.catalogue.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.PageRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Catalogue query")
@NoArgsConstructor
@Getter
@Setter
public class CatalogueDraftQuery extends PageRequestDto {

    @Builder
    public CatalogueDraftQuery(int page, int size, String publisherKey, String query, EnumDraftStatus status) {
        super(page, size);

        this.publisherKey = publisherKey;
        this.query        = query;
        this.status       = status;
    }

    @JsonIgnore
    private String publisherKey;

    @Schema(description = "Query string used for full text search operation")
    private String query;

    @Schema(description = "Draft status")
    private EnumDraftStatus status;

    public PageRequestDto toPageRequest() {
        return PageRequestDto.of(this.page, this.size);
    }

}
