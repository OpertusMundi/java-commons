package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.DisputeStatus;

public enum EnumDisputeStatus {
    /**
     * CREATED dispute status.
     */
    CREATED,
    /**
     * PENDING_CLIENT_ACTION dispute status.
     */
    PENDING_CLIENT_ACTION,
    /**
     * SUBMITTED dispute status.
     */
    SUBMITTED,
    /**
     * PENDING_BANK_ACTION dispute status.
     */
    PENDING_BANK_ACTION,
    /**
     * REOPENED_PENDING_CLIENT_ACTION dispute status.
     */
    REOPENED_PENDING_CLIENT_ACTION,
    /**
     * CLOSED dispute status.
     */
    CLOSED
    ;

    public static EnumDisputeStatus from(DisputeStatus s) throws PaymentException {
        for (final EnumDisputeStatus item : EnumDisputeStatus.values()) {
            if (item.name().equalsIgnoreCase(s.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Dispute status [%s] is not supported", s)
        );
    }

}
