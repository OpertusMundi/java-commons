package eu.opertusmundi.common.model.order;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@Builder
@JsonIgnoreType
public class OrderCommand {

    private Integer userId;

    private Integer cartId;

    private CatalogueItemDto asset;

    private EffectivePricingModelDto quotation;

    private EnumDeliveryMethod deliveryMethod;

    private Location location;

}
