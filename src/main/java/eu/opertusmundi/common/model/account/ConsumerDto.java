package eu.opertusmundi.common.model.account;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class ConsumerDto {

    @JsonIgnore
    private Integer id;

    @Schema(description = "Consumer unique key")
    private UUID key;

    @Schema(description = "Consumer name")
    private String name;

    @Schema(description = "Consumer country")
    private String country;

}