package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.transform.EnumTransformSource;
import eu.opertusmundi.common.model.transform.ServerStatusResponseDto;
import eu.opertusmundi.common.model.transform.ServerTransformDeferredResponseDto;
import eu.opertusmundi.common.model.transform.TransformServiceException;

public interface TransformService {

    void transformSync(
        EnumTransformSource sourceType, String source, String format, String sourceCrs, String target, String targetCrs
    ) throws TransformServiceException;

    ServerTransformDeferredResponseDto transformAsync(
        EnumTransformSource sourceType, String source, String format, String sourceCrs, String targetCrs
    ) throws TransformServiceException;

    ServerStatusResponseDto getStatus(String ticket) throws TransformServiceException;

    void getResource(String ticket, String target) throws TransformServiceException;

}
