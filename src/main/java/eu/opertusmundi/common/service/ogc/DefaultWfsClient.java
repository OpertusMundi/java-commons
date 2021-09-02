package eu.opertusmundi.common.service.ogc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;

@Service
public class DefaultWfsClient implements WfsClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWfsClient.class);

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private XmlMapper xmlMapper;

    @Override
    public ServiceResourceDto getMetadata(
        String endpoint, String workspace, String typeName, String userName, String password
    ) throws OgcServiceClientException {
        try {
            final ServiceResourceDto resource = new ServiceResourceDto();

            this.getServiceMetadata(endpoint, workspace, typeName, userName, password, resource);
            // this.getFeatureTypeMetadata(endpoint, workspace, typeName, userName, password, resource);

            return resource;
        } catch (final OgcServiceClientException ex) {
            throw ex;
        } catch (final Exception ex) {
            this.handleException(ex);
        }
        return null;
    }

    private void getServiceMetadata(
        String endpoint, String workspace, String typeName, String userName, String password, ServiceResourceDto resource
    ) throws Exception {
        final URI uri = new URIBuilder(endpoint)
                .clearParameters()
                .addParameter("SERVICE", "WFS")
                .addParameter("REQUEST", "GetCapabilities")
                .addParameter("VERSION", "2.0.0")
                .build();

        final RequestBuilder builder = this.getBuilder(HttpMethod.GET, uri);
        final HttpUriRequest request = builder.build();

        try (CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
            final int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                try (InputStream contentStream = response.getEntity().getContent()) {
                    final ServerGetCapabilitiesDto result = this.xmlMapper.readValue(contentStream, ServerGetCapabilitiesDto.class);

                    final ServerGetCapabilitiesDto.WGS84BoundingBox bbox = result.getBoundingBox(workspace, typeName);
                    if (bbox != null) {
                        resource.setBbox(this.getBoundingBox(bbox));
                    }

                    resource.setCrs(result.getCrs(workspace, typeName));
                    resource.setFilterCapabilities(result.getFilterCapabilities());
                    resource.setOutputFormats(result.getOutputFormats());
                    resource.setServiceType(EnumSpatialDataServiceType.WFS);
                }
            } else {
                throw new OgcServiceClientException(
                    OgcServiceMessageCode.WFS_SERVICE_ERROR,
                    String.format("Request has failed. [code=%s, reason=%s]",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase()
                    )
                );
            }
        }
    }

    @SuppressWarnings("unused")
    private void getFeatureTypeMetadata(String endpoint, String workspace, String typeName, String userName, String password, ServiceResourceDto resource) throws Exception {
        final URI uri = new URIBuilder(endpoint)
                .clearParameters()
                .addParameter("SERVICE", "WFS")
                .addParameter("REQUEST", "DescribeFeatureType")
                .addParameter("VERSION", "2.0.0")
                .addParameter("TYPENAME", typeName)
                .build();

        final RequestBuilder reqBuilder = this.getBuilder(HttpMethod.GET, uri);
        final HttpUriRequest request    = reqBuilder.build();

        try (CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
            final int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                try (InputStream contentStream = response.getEntity().getContent()) {
                    final DocumentBuilderFactory factory    = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder        docBuilder = factory.newDocumentBuilder();
                    final Document               doc        = docBuilder.parse(contentStream);

                    doc.getDocumentElement().normalize();

                    final XPath    xPath      = XPathFactory.newInstance().newXPath();
                    final String   expression = "//complexType//element";
                    final NodeList nodes      = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

                    for (int i = 0; i < nodes.getLength(); i++) {
                        final Node node = nodes.item(i);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            node.getAttributes().getNamedItem("name").getTextContent();
                        }
                    }
                }
            } else {
                throw new OgcServiceClientException(
                    OgcServiceMessageCode.WFS_SERVICE_ERROR,
                    String.format("Request has failed. [code=%s, reason=%s]",
                        response.getStatusLine().getStatusCode(),
                        response.getStatusLine().getReasonPhrase()
                    )
                );
            }
        }
    }

    private RequestBuilder getBuilder(HttpMethod method, URI uri) throws Exception {
        switch (method) {
            case POST :
                return RequestBuilder.post(uri);
            case GET :
                return RequestBuilder.get(uri);
            case DELETE :
                return RequestBuilder.delete(uri);
            default :
                throw new OgcServiceClientException(
                    OgcServiceMessageCode.HTTP_METHOD_NOT_SUPPORTED,
                    String.format("HTTP method is not supported. [method=%s]", method)
                );
        }
    }

    private Geometry getBoundingBox(ServerGetCapabilitiesDto.WGS84BoundingBox bbox) {
        final List<Double> upperCorner = bbox.getUpperCorner().getCoordinates();
        final List<Double> lowerCorner = bbox.getLowerCorner().getCoordinates();

        final double minX = lowerCorner.get(0);
        final double minY = lowerCorner.get(1);
        final double maxX = upperCorner.get(0);
        final double maxY = upperCorner.get(1);

        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        return factory.createPolygon(new Coordinate[] {
            new Coordinate(minX, minY),
            new Coordinate(maxX, minY),
            new Coordinate(maxX, maxY),
            new Coordinate(minX, maxY),
            new Coordinate(minX, minY)
        });

    }

    private void handleException(Exception ex) {
        if (ex instanceof URISyntaxException) {
            logger.error("The input is not a valid URI", ex);
            throw new OgcServiceClientException(OgcServiceMessageCode.URI_SYNTAX_ERROR, ex);
        }
        if (ex instanceof ClientProtocolException) {
            logger.error("An HTTP protocol error has occurred", ex);
            throw new OgcServiceClientException(OgcServiceMessageCode.HTTP_ERROR, ex);
        }
        if (ex instanceof IOException) {
            logger.error("An I/O exception has occurred or the connection was aborted", ex);
            throw new OgcServiceClientException(OgcServiceMessageCode.IO_ERROR, ex);
        }

        throw new OgcServiceClientException(OgcServiceMessageCode.IO_ERROR, ex);
    }

}
