package eu.opertusmundi.common.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.IngestServiceFeignClient;
import eu.opertusmundi.common.model.ingest.EnumIngestResponse;
import eu.opertusmundi.common.model.ingest.IngestServiceException;
import eu.opertusmundi.common.model.ingest.IngestServiceMessageCode;
import eu.opertusmundi.common.model.ingest.ServerIngestDeferredResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPromptResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishCommandDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestStatusResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestTicketResponseDto;
import feign.FeignException;

@Service
public class DefaultIngestService implements IngestService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultIngestService.class);

    @Value("${opertusmundi.feign.ingest.input}")
    private String inputDir;

    @Autowired
    private ObjectProvider<IngestServiceFeignClient> ingestClient;

    @Override
    public ServerIngestPromptResponseDto ingestSync(
        UUID idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException {
        try {
            final File file = new File(resource);

            if(!file.exists()) {
                throw new IngestServiceException(
                    IngestServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Resource file [%s] was not found", resource)
                );
            }
            
            final Path resolvedResourcePath = this.copyResource(idempotencyKey.toString(), resource);
            
            final ResponseEntity<ServerIngestPromptResponseDto> e = this.ingestClient.getObject().ingestSync(
                idempotencyKey, resolvedResourcePath.toString(), EnumIngestResponse.PROMPT.getValue(), schema, tablename
            );

            final ServerIngestPromptResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestDeferredResponseDto ingestAsync(
        UUID idempotencyKey, String resource, String schema, String tablename
    ) throws IngestServiceException {
        try {
            final File file = new File(resource);

            if(!file.exists()) {
                throw new IngestServiceException(
                    IngestServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Resource file [%s] was not found", resource)
                );
            }
            
            final Path resolvedResourcePath = this.copyResource(idempotencyKey.toString(), resource);
            
            final ResponseEntity<ServerIngestDeferredResponseDto> e = this.ingestClient.getObject().ingestAsync(
                idempotencyKey, resolvedResourcePath.toString(), EnumIngestResponse.DEFERRED.getValue(), schema, tablename
            );

            final ServerIngestDeferredResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestPublishResponseDto publish(
        UUID idempotencyKey, String schema, String table, String workspace
    ) throws IngestServiceException {
        try {
            final ServerIngestPublishCommandDto command = ServerIngestPublishCommandDto.builder()
                .schema(schema)
                .table(table)
                .workspace(workspace)
                .build();
            
            final ResponseEntity<ServerIngestPublishResponseDto> e = this.ingestClient.getObject().publish(
                idempotencyKey, command
            );

            final ServerIngestPublishResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }
    
    @Override
    public ServerIngestStatusResponseDto getStatus(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<ServerIngestStatusResponseDto> e = this.ingestClient.getObject().getTicketStatus(ticket);

            final ServerIngestStatusResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("[Feign Client][Ingest Service] Operation has failed", fex);

            throw new IngestServiceException(IngestServiceMessageCode.SERVICE_ERROR, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestResultResponseDto getResult(String ticket) throws IngestServiceException {
        try {
            final ResponseEntity<ServerIngestResultResponseDto> e = this.ingestClient.getObject().getTicketResult(ticket);

            final ServerIngestResultResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerIngestTicketResponseDto getTicket(UUID idempotentKey) throws IngestServiceException {
        try {
            final ResponseEntity<ServerIngestTicketResponseDto> e = this.ingestClient.getObject() .getTicketFromIdempotentKey(idempotentKey);

            final ServerIngestTicketResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("[Feign Client][Ingest Service] Operation has failed", fex);

            throw new IngestServiceException(IngestServiceMessageCode.SERVICE_ERROR, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Ingest Service] Operation has failed", ex);

            throw new IngestServiceException(IngestServiceMessageCode.UNKNOWN);
        }
    }
    
    private Path copyResource(String relativePath, String path) throws IOException {
        final String fileName           = FilenameUtils.getName(path);
        final Path   absoluteSourcePath = Paths.get(this.inputDir, relativePath, fileName);

        Files.createDirectories(Paths.get(this.inputDir, relativePath));
        Files.copy(Paths.get(path), absoluteSourcePath, StandardCopyOption.REPLACE_EXISTING);

        return Paths.get(relativePath, fileName);
    }
    
}
