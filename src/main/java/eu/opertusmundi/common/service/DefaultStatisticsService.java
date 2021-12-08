package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.CountryEntity;
import eu.opertusmundi.common.model.analytics.AssetStatisticsCommandDto;
import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.AssetStatisticsRepository;
import eu.opertusmundi.common.repository.CountryRepository;
import eu.opertusmundi.common.util.StreamUtils;

@Service
public class DefaultStatisticsService implements StatisticsService{

    private static final Logger logger = LoggerFactory.getLogger(DefaultStatisticsService.class);


    @Autowired
    private CatalogueService catalogueService;
    
    @Autowired
    private AssetStatisticsRepository assetStatisticsRepository;
    
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public AssetStatisticsDto updateStatisticsPublishAsset(String pid) {
    	AssetStatisticsCommandDto assetStatisticsCommandDto = new AssetStatisticsCommandDto();
    	
    	CatalogueItemDto cItem = this.catalogueService.findOne(null, pid, null, false); 

        final Geometry geom 				= cItem.getGeometry();
        final List<CountryEntity> countries = this.countryRepository.getCountriesWithinGeometry(geom);
        final EnumTopicCategory segment		= StreamUtils.from(cItem.getTopicCategory()).findFirst().orElse(null);    
        final ZonedDateTime publicationDate = ZonedDateTime.parse(cItem.getPublicationDate());
        final List<EffectivePricingModelDto> pricingModel = cItem.getEffectivePricingModels();
        
        BigDecimal maxPrice = new BigDecimal(0);
        for (int i = 0 ; i < pricingModel.size() ; i++) {
        	if (cItem.getEffectivePricingModels().get(i).getQuotation().getTotalPriceExcludingTax().compareTo(maxPrice) == 1) {
        		maxPrice = cItem.getEffectivePricingModels().get(i).getQuotation().getTotalPriceExcludingTax();
        	}
        }
        
        assetStatisticsCommandDto.setPid(pid);
        assetStatisticsCommandDto.setSegment(segment);
        assetStatisticsCommandDto.setPublicationDate(publicationDate);
        assetStatisticsCommandDto.setMaxPrice(maxPrice);
        assetStatisticsCommandDto.setCountries(countries);
	
    	return this.assetStatisticsRepository.create(assetStatisticsCommandDto);    	
    }

    @Override
    public void updateStatisticsUnpublishAsset(String pid) {
    	this.assetStatisticsRepository.setStatisticInactive(pid);
    }


}
