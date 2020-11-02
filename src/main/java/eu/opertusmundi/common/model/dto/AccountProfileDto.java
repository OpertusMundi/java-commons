package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileDto extends AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User addreses"
        ),
        minItems = 0
    )
    private List<AddressDto> addresses;

    @Schema(description = "Profile creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "Provider related data")
    private ProviderData provider = new ProviderData();

    @Schema(description = "Consumer related data")
    private ConsumerData consumer = new ConsumerData();

    @Schema(description = "User first name")
    private String firstName;

    @Schema(description = "User last name")
    private String lastName;

    @Schema(description = "User locale")
    private String locale;

    @Schema(description = "Profile most recent update date")
    private ZonedDateTime modifiedOn;

    @JsonIgnore
    public String getFullName() {
        if (!StringUtils.isBlank(this.firstName)) {
            if (!StringUtils.isBlank(this.lastName)) {
                return this.firstName + " " + this.lastName;
            }
            return this.firstName;
        }
        return "";
    }

    public static class ProviderData implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Current provider data. If provider is not registered, null is returned.")
        @Getter
        @Setter
        private AccountProfileProviderDto current;

        @Schema(description = "Provider draft data. If no update is active, null is returned.")
        @Getter
        @Setter
        private AccountProfileProviderDraftDto draft;

        @Schema(description = "True if the acount is a registered provider")
        public boolean isRegistered() {
            return this.current != null && this.current.getRegisteredOn() != null;
        }

    }

    public static class ConsumerData implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "Consumer related data. If the user is not a registered consumer, null is returned.")
        @Getter
        @Setter
        private AccountProfileConsumerDto current;

        @Schema(description = "True if the account is a registered consumer")
        public boolean isRegistered() {
            return this.current != null && this.current.getRegisteredOn() != null;
        }

    }

}
