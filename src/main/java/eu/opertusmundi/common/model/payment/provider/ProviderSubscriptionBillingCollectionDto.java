package eu.opertusmundi.common.model.payment.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ConsumerDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ProviderSubscriptionBillingCollectionDto extends RestResponse<PageResultDto<SubscriptionBillingDto>> {

    private ProviderSubscriptionBillingCollectionDto(PageResultDto<SubscriptionBillingDto> page) {
        super(page);

        this.subscriptions = new HashMap<UUID, ProviderAccountSubscriptionDto>();
        this.consumers     = new HashMap<UUID, ConsumerDto>();

        page.getItems().stream()
            .map(s -> (ProviderSubscriptionBillingDto) s)
            .filter(s -> s.getSubscription() != null)
            .forEach(s -> {
                if (!this.subscriptions.containsKey(s.getSubscriptionKey())) {
                    this.subscriptions.put(s.getSubscriptionKey(), s.getSubscription());
                }
                if (!this.consumers.containsKey(s.getProviderKey())) {
                    this.consumers.put(s.getConsumerKey(), s.getSubscription().getConsumer());
                    s.getSubscription().setConsumer(null);
                }
                s.setSubscription(null);
            });
    }

    public static ProviderSubscriptionBillingCollectionDto of(PageResultDto<SubscriptionBillingDto> page) {
        return new ProviderSubscriptionBillingCollectionDto(page);
    }

    @Schema(description = "Map with all subscriptions for all subscription billing records in the response. The key is the subscription key.")
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, ProviderAccountSubscriptionDto> subscriptions;

    @Schema(description = "Map with all consumers for all subscription billing records in the response. The key is the consumer id.")
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, ConsumerDto> consumers;

}
