package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.DisputeType;

public enum EnumDisputeType {
    /**
     * CONTESTABLE dispute type.
     */
    CONTESTABLE,
    /**
     * NOT_CONTESTABLE dispute type.
     */
    NOT_CONTESTABLE,
    /**
     * RETRIEVAL dispute type.
     */
    RETRIEVAL
    ;

    public static EnumDisputeType from(DisputeType t) throws PaymentException {
        for (final EnumDisputeType item : EnumDisputeType.values()) {
            if (item.name().equalsIgnoreCase(t.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Dispute type [%s] is not supported", t)
        );
    }

}
