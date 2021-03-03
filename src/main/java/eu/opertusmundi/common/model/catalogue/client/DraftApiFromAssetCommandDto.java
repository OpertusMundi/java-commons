package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class DraftApiFromAssetCommandDto extends DraftApiCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DraftApiFromAssetCommandDto() {
        super(EnumDraftCommandType.ASSET);
    }

    @Schema(description = "Published asset unique PID", required = true)
    @NotEmpty
    private String pid;

}
