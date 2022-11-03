package eu.opertusmundi.common.model.order;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@JsonIgnoreType
public class OrderCommand {

    private CatalogueItemDetailsDto  asset;
    private Integer                  cartId;
    private EnumContractType         contractType;
    private boolean                  contractUploadingRequired;
    private EnumDeliveryMethod       deliveryMethod;
    private Location                 location;
    private EffectivePricingModelDto quotation;
    private Integer                  userId;
    private boolean                  vettingRequired;

}
