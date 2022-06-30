package eu.opertusmundi.common.model.payment;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class BillingSubscriptionDates {

    private final LocalDate dateDue;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final LocalDate dateStatsReady;

}
