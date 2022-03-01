package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class AccountRecentSearchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;

    private ZonedDateTime addedOn;

}
