package eu.opertusmundi.common.service.messaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import eu.opertusmundi.common.model.email.AttachmentDto;
import eu.opertusmundi.common.model.email.EmailAddressDto;

public class MailModelBuilder {

    private final Map<String, Object> model = new HashMap<>();

    private String recipientAddress;

    private String recipientName;
    
    private List<AttachmentDto> attachments;

    private MailModelBuilder() {

    }

    public static MailModelBuilder builder() {
        return new MailModelBuilder();
    }

    public Object get(String name) {
        return this.model.get(name);
    }

    public EmailAddressDto getAddress() {
        if (StringUtils.isBlank(recipientName)) {
            return EmailAddressDto.of(recipientAddress);
        } else {
            return EmailAddressDto.of(recipientAddress, recipientName);
        }
    }

    public MailModelBuilder add(String name, Object value) {
        this.model.put(name, value);
        return this;
    }

    public MailModelBuilder addAll(Map<String, Object> values) {
        values.keySet().stream().forEach(key -> this.model.put(key, values.get(key)));
        return this;
    }

    public MailModelBuilder setRecipientAddress(String value) {
        this.recipientAddress = value;
        return this;
    }

    public MailModelBuilder setRecipientName(String value) {
        this.recipientName = value;
        return this;
    }
    
    public void addAttachment(String fileAbsolutePath) throws IOException {
    	AttachmentDto attachment = new AttachmentDto(FilenameUtils.getName(fileAbsolutePath), Files.readAllBytes(Paths.get(fileAbsolutePath)), MediaType.APPLICATION_PDF_VALUE);
        this.attachments.add(attachment);
    }
    
    public MailModelBuilder addAllAttachments(Map<String, Object> values) throws IOException {
        for (String key : values.keySet()) {
        	// If a variable name contains the word attachment, it is an attachment
        	if (key.contains("attachment")) {
        		this.addAttachment(key);
        	}
        }
        return this;
    }

    public Map<String, Object> build() {
        return this.model;
    } 

}
