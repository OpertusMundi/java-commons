package eu.opertusmundi.common.model.account;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AccountClientCommandDto {

    @JsonIgnore
    private Integer accountId;

    /**
     * Optional client id for internal use only
     * 
     * <p>
     * If not set a random {@link UUID} is generated.
     */
    @JsonIgnore
    @Builder.Default
    private Optional<UUID> clientId = Optional.empty();
    
    @Schema(description = "User-defined client name")
    @NotEmpty
    private String alias;

}
