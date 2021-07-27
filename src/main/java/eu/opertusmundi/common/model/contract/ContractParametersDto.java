package eu.opertusmundi.common.model.contract;

import java.nio.file.Path;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ContractParametersDto {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Provider {
		private String corporateName;
		private String professionalAddress;
		private String contactEmail;
		private String contactPerson;
		private String copmanyRegistrationNumber;
		private String euVatNumber;
	};

	private Provider provider;

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Consumer {
		private String corporateName;
		private String professionalAddress;
		private String contactEmail;
		private String contactPerson;
		private String copmanyRegistrationNumber;
		private String euVatNumber;
	};

	private Consumer consumer;

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Product {
		private String productID;
		private String productName;
		private String productDescription;
		private String pastVersionIncluded;
		private String updatesIncluded;
		private String estimatedDeliveryDate;
		private String mediaAndFormatOfDelivery;
		private String applicableFees;
	};

	private Product product;

	public ContractParametersDto() {

	}

}
