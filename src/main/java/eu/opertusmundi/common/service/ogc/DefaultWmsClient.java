package eu.opertusmundi.common.service.ogc;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.geotools.ows.wms.CRSEnvelope;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
import org.geotools.ows.wms.xml.Dimension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto.Attributes;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;

@Service
public class DefaultWmsClient implements WmsClient {

    @Override
    public ServiceResourceDto GetMetadata(URL url, String typeName, String userName, String password) throws OgcServiceClientException {
        try {
            final WebMapServer wms = new WebMapServer(url);

            final WMSCapabilities capabilities = wms.getCapabilities();

            return Arrays.asList(WMSUtils.getNamedLayers(capabilities)).stream()
                .filter(l -> l.getName().equals(typeName))
                .map(l -> {
                    @SuppressWarnings("unchecked")
                    final List<String> styles = (List<String>) l.getStyles().stream()
                        .flatMap(s1 -> s1.getLegendURLs().stream())
                        .map(s2 -> s2.toString())
                        .collect(Collectors.toList());

                    final List<ServiceResourceDto.Dimension> dimensions = l.getDimensions().keySet().stream().map(name -> {
                        final Dimension d = l.getDimension(name);

                        return ServiceResourceDto.Dimension.builder()
                            .name(d.getName())
                            .unit(d.getUnits())
                            .build();
                    }).collect(Collectors.toList());

                    final Double minScale = Double.isNaN(l.getScaleDenominatorMin())
                        ? null
                        : Double.valueOf(l.getScaleDenominatorMin());

                    final Double maxScale = Double.isNaN(l.getScaleDenominatorMax())
                        ? null
                        : Double.valueOf(l.getScaleDenominatorMax());

                    return ServiceResourceDto.builder()
                        .attributes(Attributes.builder()
                            .cascaded(l.getCascaded() != 0)
                            .queryable(l.isQueryable())
                            .build()
                        )
                        .attribution(l.getAttribution() == null ? null : l.getAttribution().getTitle())
                        .bbox(this.crsEnvelopeToGeometry(l.getLatLonBoundingBox()))
                        .crs(l.getSrs().stream().collect(Collectors.toList()))
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
                })
                .findFirst()
                .orElse(null);
        } catch (final Exception ex) {
            throw new OgcServiceClientException(OgcServiceMessageCode.UNKNOWN);
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

}
