package eu.opertusmundi.common.model.admin;

import com.mangopay.core.enumerations.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class WebhookRegistration {

    private boolean enabled;

    private EventType eventType;

    private String url;

    private boolean valid;

}
