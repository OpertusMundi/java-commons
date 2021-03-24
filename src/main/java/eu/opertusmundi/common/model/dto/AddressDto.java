package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import com.mangopay.core.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto extends AddressBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static AddressDto from(Address a) {
        final AddressDto result = new AddressDto();

        result.setCity(a.getCity());
        result.setCountry(a.getCountry().toString());
        result.setLine1(a.getAddressLine1());
        result.setLine2(a.getAddressLine2());
        result.setPostalCode(a.getPostalCode());
        result.setRegion(a.getRegion());

        return result;
    }
}
