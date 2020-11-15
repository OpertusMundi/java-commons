package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ProviderProfessionalCommandDto extends ConsumerProfessionalCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull
    private BankAccountCommandDto bankAccount;

}
