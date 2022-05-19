package eu.opertusmundi.common.model.rating;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Provider rating creation command")
@NoArgsConstructor
@Getter
@Setter
public class ProviderRatingCommandDto extends BaseRatingCommandDto {

    @JsonIgnore
    private UUID provider;

}
