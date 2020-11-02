package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileProviderFileDto {

    @JsonIgnore
    private int id;

    @JsonIgnore
    private SimplAccountDto createdBy;

    private ZonedDateTime createdAt;

    private FileUploadDto file;

}
