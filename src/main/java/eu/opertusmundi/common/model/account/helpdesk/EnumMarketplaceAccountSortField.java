package eu.opertusmundi.common.model.account.helpdesk;

import lombok.Getter;

public enum EnumMarketplaceAccountSortField {
    EMAIL("email"),
    CONSUMER_FUNDS("profile.consumer.walletFunds"),
    PROVIDER_FUNDS("profile.provider.walletFunds"),
    PROVIDER_PENDING_PAYOUT_FUNDS("profile.provider.pendingPayoutFunds"),
    ;

    @Getter
    private String value;

    private EnumMarketplaceAccountSortField(String value) {
        this.value = value;
    }

    public static EnumMarketplaceAccountSortField fromValue(String value) {
        for (final EnumMarketplaceAccountSortField e : EnumMarketplaceAccountSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Value [%s] is not a valid member of enum [EnumMarketplaceAccountSortField]", value
        ));
    }

}
