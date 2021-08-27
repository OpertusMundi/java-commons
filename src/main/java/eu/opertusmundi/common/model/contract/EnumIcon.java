package eu.opertusmundi.common.model.contract;

import lombok.Getter;

public enum EnumIcon {
	AlterationNotPermitted     ("topio-licence-terms_alteration-not-permitted.svg",        EnumIconCategory.Terms),
	AlterationPermitted        ("topio-licence-terms_alteration-permitted.svg",            EnumIconCategory.Terms),
	CommercialUseNotPermitted  ("topio-licence-terms_commercial-use-not-permitted.svg",    EnumIconCategory.Terms),
	CommercialUsePermitted     ("topio-licence-terms_commercial-use-permitted.svg",        EnumIconCategory.Terms),
	DeliveredByTopio           ("topio-licence-terms_delivered-by-topio.svg",              EnumIconCategory.Terms),
	DeliveredByVendor          ("topio-licence-terms_delivered-by-vendor.svg",             EnumIconCategory.Terms),
	DigitalDelivery            ("topio-licence-terms_digital-delivery.svg",                EnumIconCategory.Terms),
	PhysicalDelivery           ("topio-licence-terms_physical-delivery.svg",               EnumIconCategory.Terms),
	ThirdPartyNotPermitted     ("topio-licence-terms_third-party-not-permitted.svg",       EnumIconCategory.Terms),
	ThirdPartyPermitted        ("topio-licence-terms_third-party-permitted.svg",           EnumIconCategory.Terms),
	UpdatesNotIncluded         ("topio-licence-terms_updates-not-included.svg",            EnumIconCategory.Terms),
	UpdatesIncluded            ("topio-licence-terms_updates-included.svg",                EnumIconCategory.Terms),
	WarrantyNotProvided        ("topio-licence-terms_warranty-not-provided.svg",           EnumIconCategory.Terms),
	WarrantyProvided           ("topio-licence-terms_warranty-provided.svg",               EnumIconCategory.Terms),
	NoRestrictionsWorldwide    ("topio-licence-terms_no-restrictions-worldwide.svg",       EnumIconCategory.Countries),
	Geomarketing               ("topio-licence-terms_no-restrictions-worldwide.svg",       EnumIconCategory.Restrictions),
    ;

    @Getter
    private String file;

    @Getter
    private EnumIconCategory category;

    private EnumIcon(String file, EnumIconCategory category) {
        this.file     = file;
        this.category = category;
    }
}
