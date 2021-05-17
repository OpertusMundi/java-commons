package eu.opertusmundi.common.service.ogc;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.geotools.ows.wms.CRSEnvelope;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.WMSUtils;
import org.geotools.ows.wms.WebMapServer;
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
    
    public ServiceResourceDto GetMetadata(URL url, String workspace, String typeName, String userName, String password) throws OgcServiceClientException {
        try {
            final WebMapServer wms = new WebMapServer(url);
    
            final WMSCapabilities capabilities = wms.getCapabilities();
    
            return Arrays.asList(WMSUtils.getNamedLayers(capabilities)).stream()
                .filter(l -> l.getName().equals(typeName))
                .map(l -> {
                    l.getLatLonBoundingBox();
                    return ServiceResourceDto.builder()
                        .attributes(Attributes.builder()
                            .cascaded(l.getCascaded() != 0)
                            .queryable(l.isQueryable())
                            .build()
                        )
                        .bbox(this.crsEnvelopeTogeometry(l.getLatLonBoundingBox()))
                        .outputFormats(Optional
                            .ofNullable(capabilities.getRequest().getGetFeatureInfo().getFormats())
                            .orElse(Collections.emptyList())
                        )
                        .serviceType(EnumSpatialDataServiceType.WMS)
                        .build();
                })
                .findFirst()
                .orElse(null);
        } catch (final Exception ex) {
            throw new OgcServiceClientException(OgcServiceMessageCode.UNKNOWN);
        }
    }
    
    private Geometry crsEnvelopeTogeometry(CRSEnvelope e) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        return factory.createPolygon(new Coordinate[] {
            new Coordinate(e.getMinX(), e.getMinY()),
            new Coordinate(e.getMaxX(), e.getMinY()),
            new Coordinate(e.getMaxX(), e.getMaxY()),
            new Coordinate(e.getMinX(), e.getMaxY()),
            new Coordinate(e.getMinX(), e.getMinY())
        });
    }

}
