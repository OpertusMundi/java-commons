package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumProviderRegistrationFileStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderRegistrationFileDto {

    @JsonIgnore
    private int id;

    @JsonIgnore
    private SimplAccountDto modifiedBy;

    private ZonedDateTime                      createdOn;
    private FileUploadDto                      file;
    private ZonedDateTime                      modifiedOn;
    private String                             review;
    private EnumProviderRegistrationFileStatus status;

}
