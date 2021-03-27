package eu.opertusmundi.common.model.kyc;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
@ToString
@JsonIgnoreType
public class UpdateKycLevelCommand {

    private String providerUserId;

    private ZonedDateTime updatedOn;

}
