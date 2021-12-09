package eu.opertusmundi.common.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordLockDto {

    @JsonIgnore
    private long id;

    @Schema(description = "User name")
    private String ownerEmail;

    @JsonIgnore
    private int ownerId;

    @Schema(description = "User unique key")
    private UUID ownerKey;

    @JsonIgnore
    private int recordId;

    @Schema(description = "Record type")
    private EnumRecordLock recordType;

    private ZonedDateTime createdOn;

}
