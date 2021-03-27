package eu.opertusmundi.common.model.kyc;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mangopay.entities.UboDeclaration;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboDeclarationDto {

    @Schema(description = "UBO declaration unique identifier")
    private String id;

    @Schema(description = "UBO declaration status")
    private EnumUboDeclarationStatus status;

    @Schema(description = "Rejection reason type")
    public String refusedReasonType;

    @Schema(description = "Rejection reason message")
    public String refusedReasonMessage;

    @Schema(description = "Creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "Processing date")
    private ZonedDateTime processedOn;

    @JsonInclude(Include.NON_EMPTY)
    @ArraySchema(
        arraySchema = @Schema(
            description = "Declaration UBOs"
        )
    )
    private List<UboDto> ubos;

    public static UboDeclarationDto from(UboDeclaration d, boolean includeUbos) {
        UboDeclarationDto result = new UboDeclarationDto();

        result.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(d.getCreationDate()), ZoneOffset.UTC));
        result.setId(d.getId());
        if (result.getProcessedOn() != null) {
            result.setProcessedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(d.getProcessedDate()), ZoneOffset.UTC));
        }
        result.setRefusedReasonMessage(d.getMessage());
        result.setRefusedReasonType(d.getReason());
        result.setStatus(EnumUboDeclarationStatus.from(d.getStatus()));

        if (includeUbos) {
            result.setUbos(d.getUbos().stream().filter(ubo -> ubo.getActive()).map(UboDto::from).collect(Collectors.toList()));
        }

        return result;
    }

}
