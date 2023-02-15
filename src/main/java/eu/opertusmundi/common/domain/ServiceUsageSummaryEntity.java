package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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

    @Id
    @Column(name = "`start_date`", columnDefinition = "date")
    private LocalDate startDate;

    @Column(name = "`calls`")
    private Long calls;

    @Column(name = "`calls_normalized`", columnDefinition = "numeric", precision = 15, scale = 1)
    private BigDecimal callsNormalized;

    @Column(name = "`response_size_bytes`")
    private Long responseSizeInBytes;

    @Column(name = "`response_time_seconds`", columnDefinition = "numeric", precision = 15, scale = 3)
    private BigDecimal responseTimeInSeconds;

    public ServiceUsageSummaryDto toDto()
    {
        final ServiceUsageSummaryDto dto = new ServiceUsageSummaryDto(serviceKey, startDate);

        dto.setCalls(calls);
        dto.setCallsNormalized(callsNormalized);
        dto.setResponseSizeInBytes(responseSizeInBytes);
        dto.setResponseTimeInSeconds(responseTimeInSeconds);

        return dto;
    }
}