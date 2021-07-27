package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerRrepresentativeEmbeddable;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
import eu.opertusmundi.common.model.contract.ContractParametersDto.Consumer;
import eu.opertusmundi.common.model.contract.ContractParametersDto.Product;
import eu.opertusmundi.common.model.contract.ContractParametersDto.Provider;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.service.CatalogueService;

@Service
public class DefaultConstractOrderInformationService implements ContractOrderInformationService{

	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
	CatalogueService catalogueService;
	
	@Override
	public Consumer getConsumerInformation(UUID orderKey) {
		OrderEntity order 									= orderRepository.findOrderEntityByKey(orderKey).get();
		AccountEntity consumerAccount 						= order.getConsumer();
		CustomerDraftProfessionalEntity professionalEntity 	= consumerAccount.getProviderRegistration();
		String corporateName 								= professionalEntity.getName();
		CustomerRrepresentativeEmbeddable representative 	= professionalEntity.getRepresentative();
		String contactPerson 								= representative.getFirstName() + " " + representative.getLastName();
		String companyRegistrationNumber 					= professionalEntity.getCompanyNumber();
		String euVATNumber 									= "098765432";
		String contactEmail 								= professionalEntity.getEmail();
		AddressEmbeddable consumerAddressObj 				= professionalEntity.getHeadquartersAddress();
		String professionalAddress 							= consumerAddressObj.getLine1() + " " + consumerAddressObj.getLine2() + ", " +  
					consumerAddressObj.getPostalCode() + ", " + consumerAddressObj.getRegion() + ", "  + consumerAddressObj.getCity();
		Consumer consumer									= new Consumer(corporateName, professionalAddress,
					contactEmail, contactPerson, companyRegistrationNumber, euVATNumber);
		return consumer;
	}

	@Override
	public Provider getProviderInformation(UUID orderKey) {
		OrderEntity order 									= orderRepository.findOrderEntityByKey(orderKey).get();
		OrderItemEntity item 								=  order.getItems().get(0);
		AccountEntity providerAccount 						= item.getProvider();
		CustomerDraftProfessionalEntity professionalEntity 	= providerAccount.getProviderRegistration();
		String corporateName 								= professionalEntity.getName();
		CustomerRrepresentativeEmbeddable representative 	= professionalEntity.getRepresentative();
		String contactPerson 								= representative.getFirstName() + " " + representative.getLastName();
		String companyRegistrationNumber 					= professionalEntity.getCompanyNumber();
		String euVATNumber 									= "098765432";
		String contactEmail 								= professionalEntity.getEmail();
		AddressEmbeddable consumerAddressObj 				= professionalEntity.getHeadquartersAddress();
		String professionalAddress 							= consumerAddressObj.getLine1() + " " + consumerAddressObj.getLine2() + ", " +  
					consumerAddressObj.getPostalCode() + ", " + consumerAddressObj.getRegion() + ", "  + consumerAddressObj.getCity();
		Provider provider 									= new Provider(corporateName, professionalAddress, 
					contactEmail, contactPerson, companyRegistrationNumber, euVATNumber);
		
		return provider;
	}

	@Override
	public Product getProductInformation(UUID orderKey) {
		OrderEntity order		=	orderRepository.findOrderEntityByKey(orderKey).get();
		OrderItemEntity item	=	order.getItems().get(0);
		String assetId			=	item.getAssetId();
		CatalogueFeature catalogueFeature = catalogueService.findOneFeature(assetId);
		CatalogueFeatureProperties itemProperties = catalogueFeature.getProperties();
		Product product = new Product(assetId, itemProperties.getTitle(), itemProperties.getAbstractText(), 
				assetId, assetId, assetId, itemProperties.getDeliveryMethod(), assetId);
		return product;
	}

}
