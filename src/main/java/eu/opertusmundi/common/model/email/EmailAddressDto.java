package eu.opertusmundi.common.model.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Email address")
@NoArgsConstructor
public class EmailAddressDto {

    @Schema(description = "Contract name", required = false)
    @Getter
    @Setter
    private String name;

    @Schema(description = "Email address", required = true)
    @Getter
    @Setter
    private String address;

    public EmailAddressDto(String address) {
        this.address = address;
    }

    public EmailAddressDto(String address, String name) {
        this.address = address;
        this.name    = name;
    }

    public static EmailAddressDto of(String address) {
        final EmailAddressDto a = new EmailAddressDto();
        a.address = address;
        return a;
    }

    public static EmailAddressDto of(String address, String name) {
        final EmailAddressDto a = new EmailAddressDto();
        a.address = address;
        a.name    = name;
        return a;
    }

    @Override
    public String toString() {
        return "EmailAddress [name=" + this.name + ", address=" + this.address + "]";
    }

}
