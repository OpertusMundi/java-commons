package eu.opertusmundi.common.service;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.profiler.DataProfilerDeferredResponseDto;
import eu.opertusmundi.common.model.profiler.DataProfilerOptions;
import eu.opertusmundi.common.model.profiler.DataProfilerServiceException;
import eu.opertusmundi.common.model.profiler.DataProfilerStatusResponseDto;

public interface DataProfilerService {

    DataProfilerDeferredResponseDto profile(
        String idempotencyKey, EnumAssetType type, String resource, DataProfilerOptions options
    ) throws DataProfilerServiceException;

    DataProfilerStatusResponseDto getStatus(String ticket) throws DataProfilerServiceException;

    JsonNode getMetadata(String ticket) throws DataProfilerServiceException;


}
