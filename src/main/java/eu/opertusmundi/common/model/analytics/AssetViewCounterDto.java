package eu.opertusmundi.common.model.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AssetViewCounterDto {

    private AssetViewCounterDto(String pid, int count) {
        this(pid, count, null);
    }

    private AssetViewCounterDto(String pid, int count, CatalogueItemDetailsDto asset) {
        this.pid   = pid;
        this.count = count;
    }

    public static AssetViewCounterDto of(String pid, int count) {
        final AssetViewCounterDto a = new AssetViewCounterDto();
        a.pid   = pid;
        a.count = count;
        return a;
    }

    @Schema(description = "Asset unique PID")
    private String pid;

    @Schema(description = "Number of asset views")
    private int    count;

    @Schema(description = "Optional asset details")
    @JsonInclude(Include.NON_NULL)
    @Setter
    private CatalogueItemDetailsDto asset;

}
