package eu.opertusmundi.common.model.kyc;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.KycDocument;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycDocumentDto {

    @Schema(description = "Document unique identifier")
    private String id;

    @Schema(description = "The type of the KYC document")
    private EnumKycDocumentType type;

    @JsonIgnore
    private String tag;

    @Schema(description = "Document status")
    private EnumKycDocumentStatus status;

    @Schema(description = "Document rejection reason type")
    private String refusedReasonType;

    @Schema(description = "Document rejection reason message")
    private String refusedReasonMessage;

    @Schema(description = "Creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "Processing date")
    private ZonedDateTime processedOn;

    public static KycDocumentDto from(KycDocument d) {
        final KycDocumentDto result = new KycDocumentDto();

        result.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(d.getCreationDate()), ZoneOffset.UTC));
        result.setId(d.getId());
        if (result.getProcessedOn() != null) {
            result.setProcessedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(d.getProcessedDate()), ZoneOffset.UTC));
        }
        result.setRefusedReasonMessage(d.getRefusedReasonMessage());
        result.setRefusedReasonType(d.getRefusedReasonType());
        result.setStatus(EnumKycDocumentStatus.from(d.getStatus()));

        result.setTag(d.getTag());
        result.setType(EnumKycDocumentType.from(d.getType()));

        return result;
    }

}
