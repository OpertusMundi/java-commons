package eu.opertusmundi.common.service;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.profiler.DataProfilerDeferredResponseDto;
import eu.opertusmundi.common.model.profiler.DataProfilerServiceException;
import eu.opertusmundi.common.model.profiler.DataProfilerStatusResponseDto;
import eu.opertusmundi.common.model.profiler.EnumDataProfilerSourceType;

public interface DataProfilerService {

    DataProfilerDeferredResponseDto profile(EnumDataProfilerSourceType type, String source) throws DataProfilerServiceException;

    DataProfilerStatusResponseDto getStatus(String ticket) throws DataProfilerServiceException;

    JsonNode getMetadata(String ticket) throws DataProfilerServiceException;


}
