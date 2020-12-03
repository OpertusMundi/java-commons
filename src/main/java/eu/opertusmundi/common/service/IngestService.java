package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ingest.ClientEndpointsDto;
import eu.opertusmundi.common.model.ingest.ClientStatusDto;
import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;

public interface IngestService {

    ClientEndpointsDto ingestSync(String source) throws IngestServiceException;

    ServerIngestDeferredResponseDto ingestAsync(String source) throws IngestServiceException;

    ClientStatusDto getStatus(String ticket) throws IngestServiceException;

    ClientEndpointsDto getEndpoints(String ticket) throws IngestServiceException;
}
