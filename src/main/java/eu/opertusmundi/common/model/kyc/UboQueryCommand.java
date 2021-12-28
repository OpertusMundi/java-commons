package eu.opertusmundi.common.model.kyc;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.account.EnumCustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@JsonIgnoreType
public class UboQueryCommand {

    private UUID customerKey;

    private EnumCustomerType customerType;

    private Set<EnumUboDeclarationStatus> status;

    private int pageIndex;

    private int pageSize;

}
