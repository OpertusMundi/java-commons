package eu.opertusmundi.common.model.location;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Country ISO 3166-1 alpha-2 code
     */
    private String country;

    private String province;

    private String city;

    private String latitude;

    private String longitude;

    private String timezone;

    private String ip;

    public static Location empty(String ip) {
        return Location.empty(ip, null);
    }

    public static Location empty(String ip, String country) {
        return Location.builder()
            .country(country)
            .ip(ip)
            .build();
    }


}
