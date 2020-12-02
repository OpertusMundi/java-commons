package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileDto extends AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Consumer related data")
    private ConsumerData consumer = new ConsumerData();

    @Schema(description = "Profile creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "User first name")
    private String firstName;

    @Schema(description = "User last name")
    private String lastName;

    private String mobile;

    @Schema(description = "Profile most recent update date")
    private ZonedDateTime modifiedOn;

    @Schema(description = "Provider related data")
    private ProviderData provider = new ProviderData();

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
        private CustomerProfessionalDto current;

        @Schema(description = "Provider draft data. If no update is active, null is returned.")
        @Getter
        @Setter
        private CustomerDraftProfessionalDto draft;

        @Schema(description = "True if the account is a registered provider")
        public boolean isRegistered() {
            return this.current != null;
        }

    }

    public static class ConsumerData implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(
            description = "Current consumer data. If a consumer is not registered, null is returned.",
            oneOf = {CustomerIndividualDto.class, CustomerProfessionalDto.class}
        )
        @Getter
        @Setter
        private CustomerDto current;

        @Schema(
            description = "Consumer draft data. If no update is active, null is returned.",
            oneOf = {CustomerDraftIndividualDto.class, CustomerDraftProfessionalDto.class}
        )
        @Getter
        @Setter
        private CustomerDraftDto draft;

        @Schema(description = "True if the account is a registered consumer")
        public boolean isRegistered() {
            return this.current != null;
        }

    }

}
