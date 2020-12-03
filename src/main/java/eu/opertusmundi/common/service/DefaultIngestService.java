package eu.opertusmundi.common.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.feign.client.IngestServiceFeignClient;
import eu.opertusmundi.common.model.ingest.ClientEndpointsDto;
import eu.opertusmundi.common.model.ingest.ClientStatusDto;
import eu.opertusmundi.common.model.ingest.EnumIngestResponse;
import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.IngestServiceMessageCode;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;

@Service
public class DefaultIngestService implements IngestService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultIngestService.class);

    @Autowired
    private ObjectProvider<IngestServiceFeignClient> ingestClient;

    @Override
    public ClientEndpointsDto ingestSync(String source) throws IngestServiceException {
        try {
            final File file = new File(source);

            if(!file.exists()) {
                throw new IngestServiceException(
                    IngestServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }
            final ResponseEntity<JsonNode> e = this.ingestClient.getObject().ingest(
                file, EnumIngestResponse.PROMPT.getValue()
            );

            final JsonNode serviceResponse = e.getBody();

            return ClientEndpointsDto.fromJsonNode(serviceResponse);
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
            final ResponseEntity<JsonNode> e = this.ingestClient.getObject().ingest(
                file, EnumIngestResponse.DEFERRED.getValue()
            );

            final JsonNode serviceResponse = e.getBody();

            return ServerIngestDeferredResponseDto.fromJsonNode(serviceResponse);
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ClientStatusDto getStatus(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<JsonNode> e = this.ingestClient.getObject().getStatus(ticket);

            final JsonNode serviceResponse = e.getBody();

            return ClientStatusDto.fromJsonNode(serviceResponse);
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ClientEndpointsDto getEndpoints(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<JsonNode> e = this.ingestClient.getObject().getEndpoints(ticket);

            final JsonNode serviceResponse = e.getBody();

            return ClientEndpointsDto.fromJsonNode(serviceResponse);
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

}
