
package eu.opertusmundi.common.xjc.generated.vies;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 *
 */
@WebService(name = "checkVatPortType", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface CheckVatPortType {


    /**
     *
     * @param valid
     * @param address
     * @param countryCode
     * @param requestDate
     * @param name
     * @param vatNumber
     */
    @WebMethod
    @RequestWrapper(localName = "checkVat", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", className = "eu.opertusmundi.common.xjc.generated.vies.CheckVat")
    @ResponseWrapper(localName = "checkVatResponse", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", className = "eu.opertusmundi.common.xjc.generated.vies.CheckVatResponse") void checkVat(
        @WebParam(name = "countryCode", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> countryCode,
        @WebParam(name = "vatNumber", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> vatNumber,
        @WebParam(name = "requestDate", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<XMLGregorianCalendar> requestDate,
        @WebParam(name = "valid", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<Boolean> valid,
        @WebParam(name = "name", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> name,
        @WebParam(name = "address", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> address);

    /**
     *
     * @param traderStreetMatch
     * @param traderCityMatch
     * @param traderPostcode
     * @param traderPostcodeMatch
     * @param traderStreet
     * @param traderNameMatch
     * @param traderAddress
     * @param valid
     * @param traderCompanyType
     * @param countryCode
     * @param traderCity
     * @param requestDate
     * @param requesterVatNumber
     * @param requestIdentifier
     * @param traderCompanyTypeMatch
     * @param requesterCountryCode
     * @param vatNumber
     * @param traderName
     */
    @WebMethod
    @RequestWrapper(localName = "checkVatApprox", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", className = "eu.opertusmundi.common.xjc.generated.vies.CheckVatApprox")
    @ResponseWrapper(localName = "checkVatApproxResponse", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", className = "eu.opertusmundi.common.xjc.generated.vies.CheckVatApproxResponse") void checkVatApprox(
        @WebParam(name = "countryCode", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> countryCode,
        @WebParam(name = "vatNumber", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> vatNumber,
        @WebParam(name = "traderName", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> traderName,
        @WebParam(name = "traderCompanyType", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> traderCompanyType,
        @WebParam(name = "traderStreet", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> traderStreet,
        @WebParam(name = "traderPostcode", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> traderPostcode,
        @WebParam(name = "traderCity", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.INOUT)
        Holder<String> traderCity,
        @WebParam(name = "requesterCountryCode", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types")
        String requesterCountryCode,
        @WebParam(name = "requesterVatNumber", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types")
        String requesterVatNumber,
        @WebParam(name = "requestDate", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<XMLGregorianCalendar> requestDate,
        @WebParam(name = "valid", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<Boolean> valid,
        @WebParam(name = "traderAddress", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderAddress,
        @WebParam(name = "traderNameMatch", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderNameMatch,
        @WebParam(name = "traderCompanyTypeMatch", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderCompanyTypeMatch,
        @WebParam(name = "traderStreetMatch", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderStreetMatch,
        @WebParam(name = "traderPostcodeMatch", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderPostcodeMatch,
        @WebParam(name = "traderCityMatch", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> traderCityMatch,
        @WebParam(name = "requestIdentifier", targetNamespace = "urn:ec.europa.eu:taxud:vies:services:checkVat:types", mode = WebParam.Mode.OUT)
        Holder<String> requestIdentifier);

}
