package eu.opertusmundi.common.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoIpApiLocation {

    /**
     * Country ISO 3166-1 alpha-2 code
     */
    private String country;

    @JsonProperty("stateprov")
    private String province;

    private String city;

    private String latitude;

    private String longitude;

    private String timezone;

    public  Location toLocation(String ip) {
        return Location.builder()
            .country(country)
            .ip(ip)
            .province(province)
            .city(city)
            .longitude(longitude)
            .latitude(latitude)
            .timezone(timezone)
            .build();
    }
}
