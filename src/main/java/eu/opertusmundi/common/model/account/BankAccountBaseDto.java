package eu.opertusmundi.common.model.account;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountBaseDto {

    @JsonInclude(Include.NON_EMPTY)
    protected String id;

    @NotEmpty
    protected String ownerName;

    @NotEmpty
    protected String iban;

    @NotEmpty
    protected String bic;

}
