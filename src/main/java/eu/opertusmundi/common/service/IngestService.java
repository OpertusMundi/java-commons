package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPromptResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestTicketResponseDto;

public interface IngestService {

    default ServerIngestPromptResponseDto ingestSync(
        UUID idempotencyKey, String resource, String tablename
    ) {
        return this.ingestSync(idempotencyKey, resource, null, tablename);
    }
    
    ServerIngestPromptResponseDto ingestSync(
        UUID idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException;

    default ServerIngestDeferredResponseDto ingestAsync(
        UUID idempotencyKey, String resource, String tablename
    ) {
        return this.ingestAsync(idempotencyKey, resource, null, tablename);
    }       
    
    ServerIngestDeferredResponseDto ingestAsync(
        UUID idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException;

    default ServerIngestPublishResponseDto publish(UUID idempotencyKey, String table) {
        return this.publish(idempotencyKey, null, table, null);
    }       
    
    ServerIngestPublishResponseDto publish(
        UUID idempotencyKey, String schema, String table, String workspace
    ) throws IngestServiceException;

    ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException;

    ServerIngestResultResponseDto getResult(String ticket) throws IngestServiceException;
    
    ServerIngestTicketResponseDto getTicket(UUID idempotentKey) throws IngestServiceException;
    
}
