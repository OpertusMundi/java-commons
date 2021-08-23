package eu.opertusmundi.common.model.contract;

import lombok.Getter;

public enum EnumIcon {
	AlterationNotPermitted("topio-licence-terms_alteration-not-permitted.svg"),
	AlterationPermitted("topio-licence-terms_alteration-permitted.svg"),
	CommercialUseNotPermitted("topio-licence-terms_commercial-use-not-permitted.svg"),
	CommercialUsePermitted("topio-licence-terms_commercial-use-permitted.svg"),
	DeliveredByTopio("topio-licence-terms_delivered-by-topio.svg"),
	DeliveredByVendor("topio-licence-terms_delivered-by-vendor.svg"),
	DigitalDelivery("topio-licence-terms_digital-delivery.svg"),
	PhysicalDelivery("topio-licence-terms_physical-delivery.svg"),
	ThirdPartyNotPermitted("topio-licence-terms_third-party-not-permitted.svg"),
	ThirdPartyPermitted("topio-licence-terms_third-party-permitted.svg"),
	UpdatesNotIncluded("topio-licence-terms_updates-not-included.svg"),
	UpdatesIncluded("topio-licence-terms_updates-included.svg"),
	WarrantyNotProvided("topio-licence-terms_warranty-not-provided.svg"),
	WarrantyProvided("topio-licence-terms_warranty-provided.svg"),
	NoRestrictionsWorldwide("topio-licence-terms_no-restrictions-worldwide.svg"),
	;
	
	@Getter
    private String file;

    private EnumIcon(String file) {
        this.file = file;
    }
}
