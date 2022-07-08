package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import eu.opertusmundi.common.model.asset.EnumResourceSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DraftApiFromFileCommandDto extends DraftApiCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DraftApiFromFileCommandDto() {
        super(EnumDraftCommandType.FILE);
    }

    @Schema(description = "Geometry data CRS")
    private String crs;

    @Schema(description = "File encoding")
    private String encoding;

    @Schema(description = "File format", required = true)
    @NotEmpty
    private String format;

    @Schema(description = "Path to user's file system", required = true)
    @NotEmpty
    private String path;

    public EnumResourceSource getSource() {
        return EnumResourceSource.FILE_SYSTEM;
    }

}
