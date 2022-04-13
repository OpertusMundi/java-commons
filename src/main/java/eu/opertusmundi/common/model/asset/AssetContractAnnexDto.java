package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter(AccessLevel.PROTECTED)
public class AssetContractAnnexDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Annex resource file unique identifier")
    @Getter
    private String id;

    @Schema(description = "The description of the file. If not set, the file name is used as text")
    @Getter
    private String description;

    @Schema(description = "The file name")
    @Getter
    private String fileName;

    @Schema(description = "File size")
    @Getter
    @Setter
    private Long size;

    @Schema(description = "Date of last update")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    public void patch(AssetContractAnnexDto r) {
        // Id and file name are immutable
        this.description = r.description;
        this.size        = r.size;
        this.modifiedOn  = r.modifiedOn;
    }

}
