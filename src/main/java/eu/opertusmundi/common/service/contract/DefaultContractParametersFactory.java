package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.ContractParametersDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.CatalogueService;

@Service
public class DefaultContractParametersFactory implements ContractParametersFactory {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    CatalogueService catalogueService;

    @Override
    public ContractParametersDto create(UUID orderKey) {
        final HelpdeskOrderDto        order    = orderRepository.findOrderObjectByKey(orderKey).get();
        final HelpdeskOrderItemDto    item     = order.getItems().get(0);
        final CustomerDto             consumer = order.getConsumer();
        final CustomerProfessionalDto provider = item.getProvider();
        final CatalogueFeature        feature  = catalogueService.findOneFeature(item.getAssetId());

        final ContractParametersDto params = ContractParametersDto.builder()
            .consumer(ContractParametersDto.Consumer.from(consumer))
            .provider(ContractParametersDto.Provider.from(provider))
            .product(ContractParametersDto.Product.from(item, feature))
            .build();

        return params;
    }

}
