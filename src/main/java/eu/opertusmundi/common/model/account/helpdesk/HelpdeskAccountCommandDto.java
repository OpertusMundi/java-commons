package eu.opertusmundi.common.model.account.helpdesk;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.account.helpdesk.HelpdeskAccountBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskAccountCommandDto extends HelpdeskAccountBaseDto implements Serializable {

	private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer id;

	private String password;

	private String passwordMatch;

}
