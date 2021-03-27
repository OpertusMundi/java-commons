package eu.opertusmundi.common.model.kyc;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycDocumentPageDto {

    @Schema(description = "Uploading date")
    private ZonedDateTime uploadedOn;

    @Schema(description = "Original file name")
    private String fileName;

    @Schema(description = "File mime type")
    private String fileType;

    @Schema(description = "File size in bytes")
    private Long fileSize;

    @JsonIgnore
    private String tag;

}
