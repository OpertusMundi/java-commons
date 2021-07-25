package eu.opertusmundi.common.service.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import eu.opertusmundi.common.model.email.EmailAddressDto;

public class MailModelBuilder {

    private final Map<String, Object> model = new HashMap<>();

    private String recipientAddress;

    private String recipientName;

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

    public Map<String, Object> build() {
        return this.model;
    }

}
