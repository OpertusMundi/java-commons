package eu.opertusmundi.common.model.file;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileCopyResourceDto extends FileCopyResourceCommandDto {

    @JsonIgnore
    private Integer id;

    private String        errorMessage;
    private UUID          idempotentKey;
    private ZonedDateTime completedOn;
    private ZonedDateTime createdOn;

}