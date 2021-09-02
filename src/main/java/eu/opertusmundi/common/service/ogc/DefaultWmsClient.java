package eu.opertusmundi.common.service.ogc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.geotools.ows.wms.CRSEnvelope;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.StyleImpl;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.xml.Dimension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto.Attributes;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;

@Service
public class DefaultWmsClient implements WmsClient {

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

            final String       prefix      = String.format("%s://%s", url.getProtocol(), url.getAuthority());
            final List<String> styles      = new ArrayList<>();
            final List<byte[]> styleImages = new ArrayList<>();

            for (final StyleImpl s : layer.getStyles()) {
                for (final Object legendUrl : s.getLegendURLs()) {
                    final String styleUrl         = (String) legendUrl;
                    final String styleRelativeUrl = styleUrl.startsWith(prefix) ? StringUtils.removeStart(styleUrl, prefix) : styleUrl;

                    styles.add(styleRelativeUrl);
                    styleImages.add(this.getImage(styleUrl));
                }
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
                .styleImages(styleImages)
                .build();
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
