package eu.opertusmundi.common.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.feign.client.DataProfilerServiceFeignClient;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.profiler.DataProfilerDeferredResponseDto;
import eu.opertusmundi.common.model.profiler.DataProfilerOptions;
import eu.opertusmundi.common.model.profiler.DataProfilerServiceException;
import eu.opertusmundi.common.model.profiler.DataProfilerServiceMessageCode;
import eu.opertusmundi.common.model.profiler.DataProfilerStatusResponseDto;
import eu.opertusmundi.common.model.profiler.EnumDataProfilerResponse;

@Service
public class DefaultDataProfilerService implements DataProfilerService{

    private static final Logger logger = LoggerFactory.getLogger(DefaultDataProfilerService.class);
   
    @Autowired
    private ObjectProvider<DataProfilerServiceFeignClient> profilerClient;

    @Override
    public DataProfilerDeferredResponseDto profile(
        EnumAssetSourceType type, String source, DataProfilerOptions options
    ) throws DataProfilerServiceException {
        try {
            final File file = new File(source);

            if(!file.exists()) {
                throw new DataProfilerServiceException(
                    DataProfilerServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }

            ResponseEntity<DataProfilerDeferredResponseDto> e = null;

            switch (type) {
                case NETCDF :
                    e = this.profilerClient.getObject().profileNetCdf(
                        file,                                           // The file
                        EnumDataProfilerResponse.DEFERRED.getValue(),   // Deferred processing mode
                        options.getBaseMapProvider(),                   // The basemap provider. Default: OpenStreetMap
                        options.getBaseMapName(),                       // The name of the basemap. Default: Mapnik
                        options.getAspectRatio(),                       // The aspect ratio of the static map to be generated
                        options.getHeight() ,                           // The height (in pixels) of the static map to be generated
                        options.getWidth(),                             // The width (in pixels) of the static map to be generated
                        options.getLat(),                               // The column name containing the latitude information
                        options.getLon(),                               // The column name containing the longitude information
                        options.getTime(),                              // The column name containing the time information
                        options.getCrs(),                               // The CRS e.g. EPSG:4326
                        options.getGeometry()                           // The column name containing the geometry information. Default: WKT
                    );
                    break;
                case RASTER :
                    e = this.profilerClient.getObject().profileRaster(file, EnumDataProfilerResponse.DEFERRED.getValue());
                    break;
                case VECTOR :
                    e = this.profilerClient.getObject().profileVector(
                        file,
                        EnumDataProfilerResponse.DEFERRED.getValue(),   // Deferred processing mode
                        options.getBaseMapProvider(),                   // The basemap provider. Default: OpenStreetMap
                        options.getBaseMapName(),                       // The name of the basemap. Default: Mapnik
                        options.getAspectRatio(),                       // The aspect ratio of the static map to be generated
                        options.getHeight() ,                           // The height (in pixels) of the static map to be generated
                        options.getWidth(),                             // The width (in pixels) of the static map to be generated
                        options.getLat(),                               // The column name containing the latitude information
                        options.getLon(),                               // The column name containing the longitude information
                        options.getTime(),                              // The column name containing the time information
                        options.getCrs(),                               // The CRS e.g. EPSG:4326
                        options.getGeometry()                           // The column name containing the geometry information. Default: WKT
                    );
                    break;
                default :
                    throw new DataProfilerServiceException(DataProfilerServiceMessageCode.SOURCE_NOT_SUPPORTED);
            }

            final DataProfilerDeferredResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch(final DataProfilerServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[Data Profiler Service] Operation has failed", ex);

            throw new DataProfilerServiceException(DataProfilerServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public DataProfilerStatusResponseDto getStatus(String ticket) throws DataProfilerServiceException {
        try {
            final ResponseEntity<DataProfilerStatusResponseDto> e = this.profilerClient.getObject().getStatus(ticket);

            final DataProfilerStatusResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Data Profiler Service] Operation has failed", ex);

            throw new DataProfilerServiceException(DataProfilerServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public JsonNode getMetadata(String ticket) throws DataProfilerServiceException {
        try {
            final ResponseEntity<JsonNode> e = this.profilerClient.getObject().getMetadata(ticket);

            final JsonNode serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Data Profiler Service] Operation has failed", ex);

            throw new DataProfilerServiceException(DataProfilerServiceMessageCode.UNKNOWN);
        }
    }

}
