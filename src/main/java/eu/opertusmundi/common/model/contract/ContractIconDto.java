package eu.opertusmundi.common.model.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Getter
@Setter
public class ContractIconDto {

    private EnumIcon icon;

    private EnumIconCategory category;

    private byte[] image;

}
