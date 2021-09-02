package eu.opertusmundi.common.model.jupyter.server;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class UserServerCommandDto 
{
	@JsonProperty("profile")
    String profileName;
}
