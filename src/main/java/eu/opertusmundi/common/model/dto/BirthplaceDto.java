package eu.opertusmundi.common.model.dto;

import com.mangopay.entities.Birthplace;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirthplaceDto {

    @Schema(description = "The city of the address")
    private String city;

    @Schema(
        description = "The country of the Address",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    private String country;

    public static BirthplaceDto from(Birthplace b) {
        final BirthplaceDto result = new BirthplaceDto();
        result.setCity(b.getCity());
        result.setCountry(b.getCountry().toString());
        return result;
    }

}
