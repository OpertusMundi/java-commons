package eu.opertusmundi.common.service.mangopay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mangopay.core.Pagination;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.HookStatus;
import com.mangopay.core.enumerations.Validity;
import com.mangopay.entities.Hook;

import eu.opertusmundi.common.model.admin.WebhookRegistration;
import eu.opertusmundi.common.model.payment.PaymentException;

/**
 * Service for configuring MANGOPAY web hooks
 */
@Service
public class MangoPayWebhookHelper extends BaseMangoPayService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayWebhookHelper.class);

    private final List<EventType> eventTypes = Arrays.asList(
        // KYC documents
        EventType.KYC_SUCCEEDED,
        EventType.KYC_FAILED,
        EventType.KYC_OUTDATED,
        // Pay-ins
        EventType.PAYIN_NORMAL_SUCCEEDED,
        EventType.PAYIN_NORMAL_FAILED,
        EventType.PAYIN_REFUND_SUCCEEDED,
        EventType.PAYIN_REFUND_FAILED,
        // Pay-outs
        EventType.PAYOUT_NORMAL_SUCCEEDED,
        EventType.PAYOUT_NORMAL_FAILED,
        EventType.PAYOUT_REFUND_SUCCEEDED,
        EventType.PAYOUT_REFUND_FAILED,
        // Transfers
        EventType.TRANSFER_NORMAL_FAILED,
        EventType.TRANSFER_NORMAL_SUCCEEDED,
        EventType.TRANSFER_REFUND_SUCCEEDED,
        EventType.TRANSFER_REFUND_FAILED,
        // UBO declarations
        EventType.UBO_DECLARATION_REFUSED,
        EventType.UBO_DECLARATION_VALIDATED,
        EventType.UBO_DECLARATION_INCOMPLETE,
        // User KYC validation status
        EventType.USER_KYC_REGULAR,
        EventType.USER_KYC_LIGHT,
        // User blocked
        EventType.USER_INFLOWS_BLOCKED,
        EventType.USER_INFLOWS_UNBLOCKED,
        EventType.USER_OUTFLOWS_BLOCKED,
        EventType.USER_OUTFLOWS_UNBLOCKED
    );

    public List<WebhookRegistration> getAll() throws PaymentException {
        try {
        // NOTE: Method getAll returns only the first 10 records
        final List<Hook> hooks = this.api.getHookApi().getAll(new Pagination(0, 100), null);

        return hooks.stream()
            .map(h -> WebhookRegistration
                .builder()
                .enabled(h.getStatus() == HookStatus.ENABLED)
                .eventType(h.getEventType())
                .url(h.getUrl())
                .valid(h.getValidity() == Validity.VALID)
                .build()
            )
            .collect(Collectors.toList());
        } catch (final Exception ex) {
            throw this.wrapException("Get Web Hooks", ex, null, logger);
        }
    }

    /**
     * Enables all registered MANGOPAY web hooks. If a web hook is not
     * registered, it is not created.
     *
     * @throws PaymentException
     */
    public void enableAll() throws PaymentException {
        this.enableAll(null);
    }

    /**
     * Register and (or) enable MANGOPAY web hooks for all supported events
     *
     * @param baseUrl
     * @throws PaymentException if action fails
     */
    public void enableAll(@Nullable String baseUrl) throws PaymentException {
        try {
            final String url = StringUtils.isBlank(baseUrl) ? null : baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "webhooks/mangopay";

            // NOTE: Method getAll returns only the first 10 records
            final List<Hook> hooks = this.api.getHookApi().getAll(new Pagination(0, 100), null);

            // Create/Enable web hook for every supported event type
            for (final EventType eventType : eventTypes) {
                Hook hook = hooks.stream()
                    .filter(h -> h.getEventType() == eventType)
                    .findFirst()
                    .orElse(null);

                if (hook == null && url != null) {
                    // Create web hook
                    hook = new Hook();
                    hook.setEventType(eventType);
                    hook.setUrl(url);

                    final Hook result = this.api.getHookApi().create(hook);
                    hooks.add(result);
                } else if (hook != null && hook.getStatus() != HookStatus.ENABLED) {
                    // Enable web hook
                    if (url != null) {
                        hook.setUrl(url);
                    }
                    hook.setStatus(HookStatus.ENABLED);

                    this.api.getHookApi().update(hook);
                }
            }
            // Check for unknown registrations
            for(final Hook hook : hooks) {
                if(!this.eventTypes.contains(hook.getEventType())) {
                    final String message = String.format(
                        "Web hook registration is not supported [eventType=%s, url=%s, status=%s]",
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
            throw this.wrapException("Setup Web Hooks", ex, null, logger);
        }
    }

    /**
     * Disable MANGOPAY web hooks for all supported events
     *
     * @throws PaymentException if action fails
     */
    public void disableAll() throws PaymentException {
        try {
            // NOTE: Method getAll returns only the first 10 records
            final List<Hook> hooks = this.api.getHookApi().getAll(new Pagination(0, 100), null);

            // Disable the web hook for every supported event type if one is
            // already registered
            for (final EventType eventType : eventTypes) {
                final Hook hook = hooks.stream()
                    .filter(h -> h.getEventType() == eventType)
                    .findFirst()
                    .orElse(null);

                if (hook != null && hook.getStatus() != HookStatus.DISABLED) {
                    // Disable web hook
                    hook.setStatus(HookStatus.DISABLED);
                    this.api.getHookApi().update(hook);
                }
            }
        } catch (final Exception ex) {
            throw this.wrapException("Disable Web Hooks", ex, null, logger);
        }
    }

}
