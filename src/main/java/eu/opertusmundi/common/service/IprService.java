package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ipr.IprServiceException;
import eu.opertusmundi.common.model.ipr.ServerIprDeferredResponseDto;
import eu.opertusmundi.common.model.ipr.ServerIprJobStatusResponseDto;

public interface IprService {

    ServerIprDeferredResponseDto embedFictitious(
        String idempotencyKey, 
        String resource, 
        String crs,
        String delimiter,
        String encoding,
        String geometry,
        String latitude,
        String longitude
    ) throws IprServiceException;

    ServerIprDeferredResponseDto embedGeometries(
        String idempotencyKey, 
        String resource, 
        String crs,
        String delimiter,
        String encoding,
        String geometry,
        String latitude,
        String longitude
    ) throws IprServiceException;

    ServerIprJobStatusResponseDto getJobStatus(String ticket, String idempotencyKey) throws IprServiceException;

}
