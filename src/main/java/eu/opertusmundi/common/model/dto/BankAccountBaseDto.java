package eu.opertusmundi.common.model.dto;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountBaseDto {

    @JsonIgnore
    protected String id;

    @NotEmpty
    protected String ownerName;

    @NotEmpty
    protected String iban;

    @NotEmpty
    protected String bic;

}
