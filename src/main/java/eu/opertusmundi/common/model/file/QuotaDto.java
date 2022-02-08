package eu.opertusmundi.common.model.file;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Getter
public class QuotaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Total available space")
    private long total;

    @Schema(description = "Space used")
    private long used;

}
