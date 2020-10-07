package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    protected Integer id;

    @Schema(description = "User locale", defaultValue = "en")
    protected String locale;

}