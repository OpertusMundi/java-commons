package eu.opertusmundi.common.service.ogc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.geotools.ows.wms.CRSEnvelope;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.StyleImpl;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.request.GetMapRequest;
import org.geotools.ows.wms.response.GetMapResponse;
import org.geotools.ows.wms.xml.Dimension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto.Attributes;
import eu.opertusmundi.common.model.catalogue.LayerStyle;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.WmsLayerSample;

@Service
public class DefaultWmsClient extends AbstractOgcClient implements WmsClient {

    @Value("${opertusmundi.wms-client.parameters.width:800}")
    private int imageWidth;

    @Autowired
    private HttpClient httpClient;

    @Override
    public ServiceResourceDto getMetadata(URL url, String typeName, String userName, String password) throws OgcServiceClientException {
        try {
            final WebMapServer    wms          = new WebMapServer(url);
            final WMSCapabilities capabilities = wms.getCapabilities();

            final Layer layer = Arrays.asList(WMSUtils.getNamedLayers(capabilities)).stream()
                .filter(l -> l.getName().equals(typeName))
                .findFirst()
                .orElse(null);

            if (layer == null) {
                return null;
            }

            final String           prefix = String.format("%s://%s", url.getProtocol(), url.getAuthority());
            final List<LayerStyle> styles = new ArrayList<>();

            for (final StyleImpl s : layer.getStyles()) {
                final LayerStyle.LayerStyleBuilder builder = LayerStyle.builder()
                    .abstractText(s.getAbstract() != null ? s.getAbstract().toString() : null)
                    .name(s.getName())
                    .title(s.getTitle() != null ? s.getTitle().toString() : null);

                final List<LayerStyle.LegendUrl> legendUrls = new ArrayList<>();

                for (final Object legendUrl : s.getLegendURLs()) {
                    final String styleUrl         = (String) legendUrl;
                    final String styleRelativeUrl = styleUrl.startsWith(prefix) ? StringUtils.removeStart(styleUrl, prefix) : styleUrl;

                    final byte[] image = this.getImage(styleUrl);

                    legendUrls.add(LayerStyle.LegendUrl.builder()
                        .image(image)
                        .url(styleRelativeUrl)
                        .build()
                    );
                }

                styles.add(builder
                    .legendUrls(legendUrls)
                    .build()
                );
            }

            final List<ServiceResourceDto.Dimension> dimensions = layer.getDimensions().keySet().stream().map(name -> {
                final Dimension d = layer.getDimension(name);

                return ServiceResourceDto.Dimension.builder()
                    .name(d.getName())
                    .unit(d.getUnits())
                    .build();
            }).collect(Collectors.toList());

            final Double minScale = Double.isNaN(layer.getScaleDenominatorMin())
                ? null
                : Double.valueOf(layer.getScaleDenominatorMin());

            final Double maxScale = Double.isNaN(layer.getScaleDenominatorMax())
                ? null
                : Double.valueOf(layer.getScaleDenominatorMax());

            return ServiceResourceDto.builder()
                .attributes(Attributes.builder()
                    .cascaded(layer.getCascaded() != 0)
                    .queryable(layer.isQueryable())
                    .build()
                )
                .attribution(layer.getAttribution() == null ? null : layer.getAttribution().getTitle())
                .bbox(this.crsEnvelopeToGeometry(layer.getLatLonBoundingBox()))
                .crs(layer.getSrs().stream().collect(Collectors.toList()))
                .dimensions(dimensions)
                .maxScale(maxScale)
                .minScale(minScale)
                .outputFormats(Optional
                    .ofNullable(capabilities.getRequest().getGetFeatureInfo().getFormats())
                    .orElse(Collections.emptyList())
                )
                .serviceType(EnumSpatialDataServiceType.WMS)
                .styles(styles)
                .build();
        } catch (final Exception ex) {
            throw new OgcServiceClientException(OgcServiceMessageCode.UNKNOWN, ex);
        }
    }

    @Override
    public List<WmsLayerSample> getSamples(
        URL url, String layerName, List<Geometry> boundaries, String userName, String password
    ) throws OgcServiceClientException {
        final List<WmsLayerSample> result  = new ArrayList<>();

        try {
            final WebMapServer         wms     = new WebMapServer(url);
            final GetMapRequest        request = wms.createGetMapRequest();

            for (final Geometry bbox : boundaries) {
                final Envelope e           = bbox.getEnvelopeInternal();
                final double   ratio       = (e.getMaxY() - e.getMinY()) / (e.getMaxX() - e.getMinX());
                final int      imageHeight = (int) (this.imageWidth * ratio);

                request.setVersion("1.1.0");
                request.setTransparent(true);
                request.setFormat(MediaType.IMAGE_PNG_VALUE);
                request.setSRS("EPSG:4326");
                request.addLayer(layerName, "");
                request.setDimensions(this.imageWidth, imageHeight);
                request.setBBox(String.format("%f,%f,%f,%f", e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()));

                final GetMapResponse response = wms.issueRequest(request);

                if(!response.getContentType().equalsIgnoreCase(MediaType.IMAGE_PNG_VALUE)) {
                    throw new OgcServiceClientException(
                        OgcServiceMessageCode.CONTENT_TYPE_NOT_SUPPORTED,
                        String.format(
                            "Content type not supported [expected=%s, received=%s]",
                            MediaType.IMAGE_PNG_VALUE, response.getContentType()
                        )
                    );
                }
                try (final InputStream in = response.getInputStream()) {
                    final byte[] image = StreamUtils.copyToByteArray(in);
                    result.add(WmsLayerSample.of(bbox, image));
                }
            }
        } catch (final OgcServiceClientException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new OgcServiceClientException(OgcServiceMessageCode.UNKNOWN, ex);
        }

        return result;
    }

    @Override
    public byte[] getMap(URL url, String layerName, String bbox, int width, int height) throws OgcServiceClientException {
        try {
            final URI uri = new URIBuilder(url.toString())
                .clearParameters()
                .addParameter("SERVICE", "WMS")
                .addParameter("VERSION", "1.1.1")
                .addParameter("REQUEST", "GetMap")
                .addParameter("FORMAT", MediaType.IMAGE_PNG_VALUE)
                .addParameter("TRANSPARENT", "true")
                .addParameter("LAYERS", layerName)
                .addParameter("SRS", "EPSG:4326")
                .addParameter("BBOX", bbox)
                .addParameter("WIDTH", Integer.toString(width))
                .addParameter("HEIGHT", Integer.toString(height))
                .build();


            final RequestBuilder builder = this.getBuilder(HttpMethod.GET, uri);
            final HttpUriRequest request = builder.build();

            try (CloseableHttpResponse response = (CloseableHttpResponse) this.httpClient.execute(request)) {
                final int status = response.getStatusLine().getStatusCode();

                if (status >= 200 && status < 300) {
                    try (InputStream in = response.getEntity().getContent()) {
                        return StreamUtils.copyToByteArray(in);
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
        } catch (final OgcServiceClientException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new OgcServiceClientException(OgcServiceMessageCode.UNKNOWN, ex);
        }
    }

    private Geometry crsEnvelopeToGeometry(CRSEnvelope e) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        return factory.createPolygon(new Coordinate[] {
            new Coordinate(e.getMinX(), e.getMinY()),
            new Coordinate(e.getMaxX(), e.getMinY()),
            new Coordinate(e.getMaxX(), e.getMaxY()),
            new Coordinate(e.getMinX(), e.getMaxY()),
            new Coordinate(e.getMinX(), e.getMinY())
        });
    }

    private byte[] getImage(String imageUrl) throws MalformedURLException, IOException {
        final URL url = new URL(imageUrl);

        try (final InputStream in = url.openStream()) {
            return StreamUtils.copyToByteArray(in);
        }
    }

}
