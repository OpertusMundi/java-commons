package eu.opertusmundi.common.model.account.helpdesk;

import eu.opertusmundi.common.model.account.helpdesk.EnumHelpdeskAccountSortField;
import lombok.Getter;

public enum EnumHelpdeskAccountSortField {
    EMAIL("email"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    CONSUMER_FUNDS("profile.consumer.walletFunds"),
    PROVIDER_FUNDS("profile.provider.walletFunds"),
    ;

    @Getter
    private String value;

    private EnumHelpdeskAccountSortField(String value) {
        this.value = value;
    }

    public static EnumHelpdeskAccountSortField fromValue(String value) {
        for (final EnumHelpdeskAccountSortField e : EnumHelpdeskAccountSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Value [%s] is not a valid member of enum [EnumHelpdeskAccountSortField]", value
        ));
    }

}
