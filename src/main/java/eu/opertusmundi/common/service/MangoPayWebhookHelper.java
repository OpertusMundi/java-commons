package eu.opertusmundi.common.service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.mangopay.core.Pagination;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.HookStatus;
import com.mangopay.entities.Hook;

/**
 * Service for configuring MANGOPAY web hooks
 */
@ConditionalOnProperty(name = "opertusmundi.payments.mangopay.web-hook.create-on-startup")
@Service
public class MangoPayWebhookHelper extends BaseMangoPayService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayWebhookHelper.class);

    @Value("${opertus-mundi.base-url}")
    private String baseUrl;

    private final List<EventType> eventTypes = Arrays.asList(
        // Pay-ins
        EventType.PAYIN_NORMAL_SUCCEEDED,
        EventType.PAYIN_NORMAL_FAILED,
        // Pay-outs
        EventType.PAYOUT_NORMAL_SUCCEEDED,
        EventType.PAYOUT_NORMAL_FAILED,
        EventType.PAYOUT_REFUND_SUCCEEDED,
        EventType.PAYOUT_REFUND_FAILED,
        // Transfers
        EventType.TRANSFER_NORMAL_CREATED,
        EventType.TRANSFER_NORMAL_FAILED,
        EventType.TRANSFER_NORMAL_SUCCEEDED,
        // KYC
        EventType.KYC_CREATED,
        EventType.KYC_SUCCEEDED,
        EventType.KYC_FAILED,
        EventType.KYC_VALIDATION_ASKED,
        EventType.KYC_OUTDATED,
        EventType.USER_KYC_REGULAR,
        EventType.USER_KYC_LIGHT
    );

    @PostConstruct
    private void init() {
        this.setup();
    }

    /**
     * Register MANGOPAY web hooks for all supported events
     */
    private void setup() {

        try {
            final String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "webhooks/mangopay";

            // NOTE: Method getAll returns only the first 10 records
            final List<Hook> hooks = this.api.getHookApi().getAll(
                new Pagination(0, 100), null
            );

            // Create/Enable web hook for every supported event type
            for (final EventType eventType : eventTypes) {
                Hook hook = hooks.stream()
                    .filter(h -> h.getEventType() == eventType)
                    .findFirst()
                    .orElse(null);

                if (hook == null) {
                    // Create web hook
                    hook = new Hook();
                    hook.setEventType(eventType);
                    hook.setUrl(url);

                    final Hook result = this.api.getHookApi().create(hook);
                    hooks.add(result);
                } else if (hook.getStatus() != HookStatus.ENABLED) {
                    // Enable web hook
                    hook.setUrl(url);
                    hook.setStatus(HookStatus.ENABLED);

                    this.api.getHookApi().update(hook);
                }
            }
            // Check for unknown registrations
            for(final Hook hook : hooks) {
                if(!this.eventTypes.contains(hook.getEventType())) {
                    final String message = String.format(
                        "Web hook registartion is not supported [eventType=%s, url=%s, status=%s]",
                        hook.getEventType(), hook.getUrl(), hook.getStatus()
                    );

                    if (hook.getStatus() == HookStatus.ENABLED) {
                        logger.error(message);
                    } else {
                        logger.warn(message);
                    }
                }
            }
        } catch (final Exception ex) {
            this.wrapException("Setup Web Hooks", ex, null, logger);
        }
    }

}