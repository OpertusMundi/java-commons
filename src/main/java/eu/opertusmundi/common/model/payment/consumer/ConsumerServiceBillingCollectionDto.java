package eu.opertusmundi.common.model.payment.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ConsumerServiceBillingCollectionDto extends RestResponse<PageResultDto<ServiceBillingDto>> {

    private ConsumerServiceBillingCollectionDto(PageResultDto<ServiceBillingDto> page) {
        super(page);

        this.services      = new HashMap<UUID, UserServiceDto>();
        this.subscriptions = new HashMap<UUID, ConsumerAccountSubscriptionDto>();
        this.publishers    = new HashMap<UUID, ProviderDto>();

        page.getItems().stream()
            .map(s -> (ConsumerServiceBillingDto) s)
            .forEach(s -> {
                if (s.getUserService() != null && !this.services.containsKey(s.getServiceKey())) {
                    this.services.put(s.getServiceKey(), s.getUserService());
                }
                if (s.getSubscription() != null && !this.subscriptions.containsKey(s.getServiceKey())) {
                    this.subscriptions.put(s.getServiceKey(), s.getSubscription());
                }
                if (s.getSubscription() != null && !this.publishers.containsKey(s.getProviderParentKey())) {
                    Assert.equals(
                        s.getSubscription().getProvider().getKey(), s.getProviderParentKey(),
                        "Expected subscription provider key to be equal to billing record provider parent key"
                    );
                    this.publishers.put(s.getProviderParentKey(), s.getSubscription().getProvider());
                    s.getSubscription().setProvider(null);
                }
                s.setSubscription(null);
                s.setUserService(null);
            });
    }

    public static ConsumerServiceBillingCollectionDto of(PageResultDto<ServiceBillingDto> page) {
        return new ConsumerServiceBillingCollectionDto(page);
    }

    @Schema(description =
        "Map with all services for all service billing records of type `PRIVATE_OGC_SERVICE` in the response. "
      + "The key is the user service key."
    )
    @Getter
    @Setter
    @JsonInclude(Include.NON_EMPTY)
    private Map<UUID, UserServiceDto> services;

    @Schema(description =
        "Map with all subscriptions for all service billing records of type `SUBSCRIPTION` in the response. "
      + "The key is the subscription key."
    )
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
