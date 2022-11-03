package eu.opertusmundi.common.model.payment.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ConsumerSubscriptionBillingCollectionDto extends RestResponse<PageResultDto<SubscriptionBillingDto>> {

    private ConsumerSubscriptionBillingCollectionDto(PageResultDto<SubscriptionBillingDto> page) {
        super(page);

        this.subscriptions = new HashMap<UUID, ConsumerAccountSubscriptionDto>();
        this.publishers    = new HashMap<UUID, ProviderDto>();

        page.getItems().stream()
            .map(s -> (ConsumerSubscriptionBillingDto) s)
            .filter(s -> s.getSubscription() != null)
            .forEach(s -> {
                if (!this.subscriptions.containsKey(s.getSubscriptionKey())) {
                    this.subscriptions.put(s.getSubscriptionKey(), s.getSubscription());
                }
                if (!this.publishers.containsKey(s.getProviderKey())) {
                    this.publishers.put(s.getProviderKey(), s.getSubscription().getProvider());
                    s.getSubscription().setProvider(null);
                }
                s.setSubscription(null);
            });
    }

    public static ConsumerSubscriptionBillingCollectionDto of(PageResultDto<SubscriptionBillingDto> page) {
        return new ConsumerSubscriptionBillingCollectionDto(page);
    }

    @Schema(description = "Map with all subscriptions for all subscription billing records in the response. The key is the subscription key.")
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, ConsumerAccountSubscriptionDto> subscriptions;

    @Schema(description = "Map with all publishers for all subscription billing records in the response. The key is the publisher id.")
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, ProviderDto> publishers;

}
