package eu.opertusmundi.common.model.order;

public enum EnumOrderStatus {
    /**
     * Order created
     */
    CREATED,
    /**
     * Order created and requires provider approval
     */
    PENDING_PROVIDER_APPROVAL,
    /**
     * Order has been rejected
     */
    PROVIDER_REJECTED,
    /**
     * Order has been accepted
     */
    PROVIDER_ACCEPTED,
    /**
     * If the order is related to a custom contract
     * the provider should fill in the contract with the consumer's info
     */
    PENDING_PROVIDER_CONTRACT_UPLOAD,
    /**
     * If the order is related to a custom contract
     * the consumer should accept the terms of the contract
     */
    PENDING_CONSUMER_CONTRACT_ACCEPTANCE,
    /**
     * Custom contract is signed on behalf of all parties by the marketplace
     */
    CONTRACT_IS_SIGNED,
    /**
     * PayIn created (order previous status must be either CREATED or
     * PROVIDER_ACCEPTED)
     */
    CHARGED,
    /**
     * Waiting for provider send confirmation
     */
    PENDING_PROVIDER_SEND_CONFIRMATION,
    /**
     * Waiting for consumer receive confirmation
     */
    PENDING_CONSUMER_RECEIVE_CONFIRMATION,
    /**
     * Order payment has been received and assets have been delivered asset.
     * Asset/subscription registration is pending
     */
    ASSET_REGISTRATION,
    /**
     * Order has been cancelled, not payment received
     */
    CANCELLED,
    /**
     * Order has been cancelled and PayIn has been refunded
     */
    REFUNDED,
    /**
     * Order has been completed
     */
    SUCCEEDED,
    ;
}
