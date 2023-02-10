package eu.opertusmundi.common.domain;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import eu.opertusmundi.common.model.account.ServiceUsageKey;
import eu.opertusmundi.common.model.account.ServiceUsageSummaryDto;

@lombok.Getter
@lombok.Setter
@Immutable
@Entity(name = "ServiceUsageSummary")
@Table(name = "`service_usage_summary`", schema = "web")
@IdClass(ServiceUsageKey.class)
public class ServiceUsageSummaryEntity
{
    @Id
    @Column(name = "`service_key`", columnDefinition = "uuid")
    private UUID serviceKey;
    
    @MapsId("serviceKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "`service_key`", nullable = true, referencedColumnName = "`key`")
    AccountSubscriptionEntity subscription;
    
    @Transient
    private UUID subscriptionKey;
    
    @MapsId("serviceKey")
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "`service_key`", nullable = true, referencedColumnName = "`key`")
    private UserServiceEntity userService;
    
    @Transient
    private UUID userServiceKey;
    
    @Id
    @Column(name = "`start_date`", columnDefinition = "date")
    private LocalDate startDate;
    
    @Column(name = "`calls`")
    private Long calls;
    
    @Column(name = "`calls_normalized`", columnDefinition = "numeric")
    private Float callsNormalized;
    
    @Column(name = "`response_size_bytes`")
    private Long responseSizeInBytes;
    
    @Column(name = "`response_time_seconds`", columnDefinition = "numeric")
    private Float responseTimeInSeconds;

    @PostLoad
    void postLoad()
    {
        if (subscription != null) {
            subscriptionKey = subscription.getKey();
        }
        
        if (userService != null) {
            userServiceKey = userService.getKey();
        }
    }
    
    public ServiceUsageSummaryDto toDto()
    {
        final ServiceUsageSummaryDto dto = new ServiceUsageSummaryDto(serviceKey, startDate);

        if (subscriptionKey != null) {
            dto.setSubscription(subscription.toConsumerDto(false));
        } else if (userServiceKey != null) {
            dto.setUserService(userService.toDto(false));
        }
        
        dto.setCalls(calls);
        dto.setCallsNormalized(callsNormalized);
        dto.setResponseSizeInBytes(responseSizeInBytes);
        dto.setResponseTimeInSeconds(responseTimeInSeconds);
        
        return dto;
    }
}