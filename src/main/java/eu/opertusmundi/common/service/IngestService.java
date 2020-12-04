package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestEndpointsResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;

public interface IngestService {

    ServerIngestEndpointsResponseDto ingestSync(String source) throws IngestServiceException;

    ServerIngestDeferredResponseDto ingestAsync(String source) throws IngestServiceException;

    ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException;

    ServerIngestEndpointsResponseDto getEndpoints(String ticket) throws IngestServiceException;
}
