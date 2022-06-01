package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPromptResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestTicketResponseDto;

public interface IngestService {

    default ServerIngestPromptResponseDto ingestSync(
        String idempotencyKey, String resource, String tablename
    ) {
        return this.ingestSync(idempotencyKey, resource, null, tablename);
    }

    ServerIngestPromptResponseDto ingestSync(
        String idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException;

    default ServerIngestDeferredResponseDto ingestAsync(
        String idempotencyKey, String resource, String tablename
    ) {
        return this.ingestAsync(idempotencyKey, resource, null, tablename);
    }

    ServerIngestDeferredResponseDto ingestAsync(
        String idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException;

    default ServerIngestPublishResponseDto publish(String idempotencyKey, String table) {
        return this.publish(idempotencyKey, null, table, null);
    }

    ServerIngestPublishResponseDto publish(
        String idempotencyKey, String schema, String table, String workspace
    ) throws IngestServiceException;

    ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException;

    ServerIngestResultResponseDto getResult(String ticket) throws IngestServiceException;

    ServerIngestTicketResponseDto getTicket(String idempotentKey) throws IngestServiceException;

    /**
     * Remove all ingested data relative to the given table
     *
     * @param table Database table name
     * @param schema The database schema; if not present the default schema will be assumed
     * @param workspace The workspace that the layer belongs; if not present, the default workspace will be assumed
     * @throws IngestServiceException
     */
    void removeLayerAndData(String table, String schema, String workspace) throws IngestServiceException;

}
