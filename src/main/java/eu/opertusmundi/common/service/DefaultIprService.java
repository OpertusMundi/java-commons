package eu.opertusmundi.common.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.IprServiceFeignClient;
import eu.opertusmundi.common.model.ipr.EnumIprResponse;
import eu.opertusmundi.common.model.ipr.IprServiceException;
import eu.opertusmundi.common.model.ipr.IprServiceMessageCode;
import eu.opertusmundi.common.model.ipr.ServerEmbedFictitiousEntriesCommandDto;
import eu.opertusmundi.common.model.ipr.ServerIprDeferredResponseDto;
import eu.opertusmundi.common.model.ipr.ServerIprJobStatusResponseDto;
import feign.FeignException;

@Service
public class DefaultIprService implements IprService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultIprService.class);

    private enum EnumOperation {
        EmbedFictitious,
        EmbedGeometries,
    }

    @Autowired
    private final ObjectProvider<IprServiceFeignClient> client;

    public DefaultIprService(Optional<ObjectProvider<IprServiceFeignClient>> client) {
        this.client = client.orElse(null);
    }

    @Override
    public ServerIprDeferredResponseDto embedFictitious(
        String idempotencyKey,
        String resource,
        String crs,
        String delimiter,
        String encoding,
        String geometry,
        String latitude,
        String longitude
    ) throws IprServiceException {
        return this.protectVector(
            EnumOperation.EmbedFictitious,
            idempotencyKey, resource, crs, delimiter, encoding, geometry, latitude, longitude
        );
    }

    @Override
    public ServerIprDeferredResponseDto embedGeometries(
        String idempotencyKey,
        String resource,
        String crs,
        String delimiter,
        String encoding,
        String geometry,
        String latitude,
        String longitude
    ) throws IprServiceException {
        return this.protectVector(
            EnumOperation.EmbedGeometries,
            idempotencyKey, resource, crs, delimiter, encoding, geometry, latitude, longitude
        );
    }

    private ServerIprDeferredResponseDto protectVector(
        EnumOperation operation,
        String        idempotencyKey,
        String        resource,
        String        crs,
        String        delimiter,
        String        encoding,
        String        geometry,
        String        latitude,
        String        longitude
    ) throws IprServiceException {
        try {
            final var command = ServerEmbedFictitiousEntriesCommandDto.builder()
                .crs(crs)
                .delimiter(delimiter)
                .encoding(encoding)
                .geom(geometry)
                .key(idempotencyKey)
                .lat(latitude)
                .lon(longitude)
                .original(resource)
                .response(EnumIprResponse.DEFERRED.getValue())
                .build();

            final ResponseEntity<ServerIprDeferredResponseDto> e = switch(operation) {
                case EmbedFictitious -> this.client.getObject().embedFictitious(idempotencyKey, command);
                case EmbedGeometries -> this.client.getObject().embedGeometries(idempotencyKey, command);
            };

            final ServerIprDeferredResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final FeignException fex) {
            logger.error(String.format("Operation has failed [response=%s]", fex.contentUTF8()), fex);

            throw new IprServiceException(IprServiceMessageCode.SERVICE_ERROR, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new IprServiceException(IprServiceMessageCode.UNKNOWN, ex);
        }
    }

    @Override
    public ServerIprJobStatusResponseDto getJobStatus(String ticket, String idempotencyKey) throws IprServiceException {
        try {
            final ResponseEntity<ServerIprJobStatusResponseDto> e = this.client.getObject().getStatus(ticket, idempotencyKey);

            final ServerIprJobStatusResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new IprServiceException(IprServiceMessageCode.SERVICE_ERROR, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new IprServiceException(IprServiceMessageCode.UNKNOWN, ex);
        }
    }

}
