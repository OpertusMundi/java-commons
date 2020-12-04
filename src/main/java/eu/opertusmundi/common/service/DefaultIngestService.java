package eu.opertusmundi.common.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.IngestServiceFeignClient;
import eu.opertusmundi.common.model.ingest.EnumIngestResponse;
import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.IngestServiceMessageCode;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestEndpointsResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;

@Service
public class DefaultIngestService implements IngestService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultIngestService.class);

    @Autowired
    private ObjectProvider<IngestServiceFeignClient> ingestClient;

    @Override
    public ServerIngestEndpointsResponseDto ingestSync(String source) throws IngestServiceException {
        try {
            final File file = new File(source);

            if(!file.exists()) {
                throw new IngestServiceException(
                    IngestServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }
            final ResponseEntity<ServerIngestEndpointsResponseDto> e = this.ingestClient.getObject().ingestSync(
                file, EnumIngestResponse.PROMPT.getValue()
            );

            final ServerIngestEndpointsResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestDeferredResponseDto ingestAsync(String source) throws IngestServiceException {
        try {
            final File file = new File(source);

            if(!file.exists()) {
                throw new IngestServiceException(
                    IngestServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }
            final ResponseEntity<ServerIngestDeferredResponseDto> e = this.ingestClient.getObject().ingestAsync(
                file, EnumIngestResponse.DEFERRED.getValue()
            );

            final ServerIngestDeferredResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<ServerIngestStatusResponseDto> e = this.ingestClient.getObject().getStatus(ticket);

            final ServerIngestStatusResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestEndpointsResponseDto getEndpoints(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<ServerIngestEndpointsResponseDto> e = this.ingestClient.getObject().getEndpoints(ticket);

            final ServerIngestEndpointsResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

}
