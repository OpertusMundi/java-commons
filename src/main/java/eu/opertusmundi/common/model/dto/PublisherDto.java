package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PublisherDto {

    @Schema(description = "Publisher unique key")
    @JsonProperty("id")
    private UUID key;

    @Schema(description = "Company name")
    private String name;

    @Schema(description = "Company location")
    private String city;

    @Schema(description = "Company country")
    private String country;

    @Schema(
        description = "Company contact email. This is the email address from the provider's profile. The email is returned only if it is verified"
    )
    private String email;

    @Schema(description = "Date of registration")
    private ZonedDateTime joinedAt;

    @Schema(description = "Company image")
    private byte[] logoImage;

    @Schema(description = "Company image mime type", example = "image/png")
    private String logoImageMimeType;

    @Schema(description = "Average rating. If no user ratings exist, null is returned", example = "3.3", minimum = "0", maximum = "5")
    private Double rating;

}
