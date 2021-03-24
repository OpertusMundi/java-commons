package eu.opertusmundi.common.model.payment;

import java.util.List;

import com.mangopay.entities.Client;

import eu.opertusmundi.common.model.dto.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto {

    @Schema(description = "A list of email addresses to use when contacting you for admin/commercial issues/communications")
    private List<String> adminEmails;

    @Schema(description = "A list of email addresses to use when contacting you for billing issues/communications")
    private List<String> billingEmails;

    @Schema(description = "An ID for the client (i.e. url friendly, lowercase etc - sort of namespace identifier)")
    private String clientId;

    @Schema(description = "A list of email addresses to use when contacting you for fraud/compliance issues/communications")
    private List<String> fraudEmails;

    @Schema(description = "The address of the company’s headquarters")
    private AddressDto headquartersAddress;

    @Schema(description = "The pretty name for the client")
    private String name;

    @Schema(description = "A list of email addresses to use when contacting you for technical issues/communications")
    private List<String> techEmails;

    public static ClientDto from(Client c) {
        final ClientDto result = new ClientDto();

        result.setAdminEmails(c.getAdminEmails());
        result.setBillingEmails(c.getBillingEmails());
        result.setClientId(c.getClientId());
        result.setFraudEmails(c.getFraudEmails());
        result.setHeadquartersAddress(AddressDto.from(c.getHeadquartersAddress()));
        result.setName(c.getName());
        result.setTechEmails(c.getTechEmails());

        return result;
    }

}