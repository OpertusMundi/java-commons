package eu.opertusmundi.common.model.file;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadDto {

    @JsonIgnore
    private Integer id;

    @JsonIgnore
    private String relativePath;

    private String             comment;
    private ZonedDateTime      createdOn;
    private String             fileName;
    private UUID               key;
    private UUID               owningEntityKey;
    private EnumFileSystemType owningEntityType;
    private Long               size;

}