package eu.opertusmundi.common.model.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Message object")
@NoArgsConstructor
public class MessageDto<M> {

    @Schema(description = "Sender", required = true)
    @NotNull
    @Getter
    private EmailAddressDto sender;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Message recipients"
        ),
        minItems = 1
    )
    @NotEmpty
    @Getter
    private List<EmailAddressDto> recipients;

    @Schema(description = "Subject", required = true)
    @Getter
    @Setter
    private String Subject;

    @Schema(description = "Unique email template id", required = true)
    @NotEmpty
    @Getter
    @Setter
    private String template;

    @Schema(description = "An serializable object with parameters for rendering the template", required = true)
    @Getter
    @Setter
    private M model;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Message attachments"
        ),
        minItems = 0
    )
    @Getter
    private final List<AttachmentDto> attachments = new ArrayList<>();

    public void setSender(String address) {
        this.sender = new EmailAddressDto(address);
    }

    public void setSender(String address, String name) {
        this.sender = new EmailAddressDto(address, name);
    }

    public void setSender(EmailAddressDto sender) {
        this.sender = sender;
    }

    public void setRecipients(String address) {
        this.recipients = Arrays.asList(new EmailAddressDto(address));
    }

    public void setRecipients(String address, String name) {
        this.recipients = Arrays.asList(new EmailAddressDto(address, name));
    }

    public void setRecipients(EmailAddressDto address) {
        this.recipients = Arrays.asList(address);
    }

    public void setRecipients(EmailAddressDto[] address) {
        this.recipients = Arrays.asList(address);
    }

    public void addAttachment(AttachmentDto attachment) {
        this.attachments.add(attachment);
    }

}
