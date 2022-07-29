package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.EnumAuthProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@JsonIgnoreType
public class ExternalIdpAccountCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder
    public ExternalIdpAccountCommand(EnumAuthProvider idpName, String email, AccountProfileCommandDto profile) {
        this.email   = email;
        this.idpName = idpName;
        this.profile = profile;
        this.type    = EnumAccountType.OPERTUSMUNDI;
    }

    private EnumAccountType type;

    private String email;

    private EnumAuthProvider idpName;

    private AccountProfileCommandDto profile;

    @Setter
    private String password;

}
