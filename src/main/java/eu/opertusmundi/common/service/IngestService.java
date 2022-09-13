package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPromptResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestTicketResponseDto;

public interface IngestService {

    ServerIngestPromptResponseDto ingestSync(
        String idempotencyKey, String resource, String shard, String workspace, String tablename
    ) throws IngestServiceException;

    ServerIngestDeferredResponseDto ingestAsync(
        String idempotencyKey, String resource, String shard, String workspace, String tablename
    ) throws IngestServiceException;

    ServerIngestPublishResponseDto publish(
        String idempotencyKey, String shard, String workspace, String table
    ) throws IngestServiceException;

    ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException;

    ServerIngestResultResponseDto getResult(String ticket) throws IngestServiceException;

    ServerIngestTicketResponseDto getTicket(String idempotentKey) throws IngestServiceException;

    /**
     * Remove all ingested data relative to the given table
     *
     * @param shard The Geoserver shard where the layer belongs to
     * @param workspace The workspace that the layer belongs
     * @param table Database table name
     * @throws IngestServiceException
     */
    void removeLayerAndData(String shard, String workspace, String table) throws IngestServiceException;

}
