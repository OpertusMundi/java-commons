package eu.opertusmundi.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import eu.europa.ec.taxation.wsdl.CheckVat;
import eu.europa.ec.taxation.wsdl.CheckVatResponse;
import eu.opertusmundi.common.domain.CountryEuropeEntity;
import eu.opertusmundi.common.model.spatial.CountryEuropeDto;
import eu.opertusmundi.common.repository.CountryRepository;

public class ViesVatClient extends WebServiceGatewaySupport {

    private final List<CountryEuropeDto> countries = new ArrayList<>();

    @Autowired
    private CountryRepository countryRepository;

    @PostConstruct
    private void init() {
        this.countryRepository.getEuropeCountries().stream()
            .map(CountryEuropeEntity::toDto)
            .forEach(this.countries::add);
    }

    @Cacheable(
        cacheNames = "company-number",
        cacheManager = "defaultCacheManager",
        key = "'company-number-' + #value"
    )
    public boolean checkVatNumber(String value) {
        final Pattern p = Pattern.compile("^[a-zA-Z]{2,}[0-9]+$");
        final Matcher m = p.matcher(value);

        if (!m.matches()) {
            return false;
        }

        final String countryCode = value.substring(0, 2);
        final String vatNumber   = value.substring(2);

        final CountryEuropeDto country = this.countries.stream()
            .filter(c -> c.getCode().equals(countryCode))
            .findFirst()
            .orElse(null);

        if (country == null) {
            return false;
        }

        return this.checkVatNumber(countryCode, vatNumber);
    }

    @Cacheable(
        cacheNames = "company-number",
        cacheManager = "defaultCacheManager",
        key = "'company-number-' + #countryCode + #vatNumber"
    )
    public boolean checkVatNumber(String countryCode, String vatNumber) {
        final CheckVat request = new CheckVat();
        request.setCountryCode(countryCode);
        request.setVatNumber(vatNumber);

        final CheckVatResponse response = (CheckVatResponse) getWebServiceTemplate().marshalSendAndReceive(
            request, new SoapActionCallback("")
        );

        return response.isValid();
    }

}
