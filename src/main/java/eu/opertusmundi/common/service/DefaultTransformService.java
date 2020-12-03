package eu.opertusmundi.common.service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.feign.client.TransformServiceFeignClient;
import eu.opertusmundi.common.model.transform.EnumSourceType;
import eu.opertusmundi.common.model.transform.EnumTransformResponse;
import eu.opertusmundi.common.model.transform.ServerStatusResponseDto;
import eu.opertusmundi.common.model.transform.ServerTransformDeferredResponseDto;
import eu.opertusmundi.common.model.transform.TransformServiceException;
import eu.opertusmundi.common.model.transform.TransformServiceMessageCode;
import feign.Response;

@Service
public class DefaultTransformService implements TransformService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTransformService.class);

    @Autowired
    private ObjectProvider<TransformServiceFeignClient> transformClient;

    @Override
    public void transformSync(
        EnumSourceType sourceType, String source, String format, String sourceCrs, String target, String targetCrs
    ) throws TransformServiceException {
        try {
            final File sourceFile = new File(source);
            final File targetFile = new File(target);

            if(!sourceFile.exists()) {
                throw new TransformServiceException(
                    TransformServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }

            if(targetFile.exists()) {
                throw new TransformServiceException(
                    TransformServiceMessageCode.TARGET_EXISTS,
                    String.format("Target file [%s] already exists", target)
                );
            }

            final Response e = this.transformClient.getObject().transformSync(
                sourceType.getValue(), format, sourceCrs, targetCrs, sourceFile, EnumTransformResponse.PROMPT.getValue()
            );

            if (e.status() != HttpStatus.OK.value()) {
                String details = "-";
                try (final InputStream input = e.body().asInputStream()) {
                    details = IOUtils.toString(input, StandardCharsets.UTF_8);
                } catch (final Exception ex) {
                    details = "<Could not parse error details>";
                }
                logger.error(
                    "[Transform Service] Operation has failed with code [{}] and reason [{}]. Additional information: {}",
                    e.status(), e.reason(), details
                );

                throw new TransformServiceException(TransformServiceMessageCode.SERVICE_ERROR);
            }

            try (final InputStream input = e.body().asInputStream()) {
                FileUtils.copyInputStreamToFile(input, targetFile);
            }
        } catch(final TransformServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[Transform Service] Operation has failed", ex);

            throw new TransformServiceException(TransformServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerTransformDeferredResponseDto transformAsync(
        EnumSourceType sourceType, String source, String format, String sourceCrs, String targetCrs
    ) throws TransformServiceException {
        try {
            final File file = new File(source);

            if(!file.exists()) {
                throw new TransformServiceException(
                    TransformServiceMessageCode.SOURCE_NOT_FOUND,
                    String.format("Source file [%s] was not found", source)
                );
            }

            final ResponseEntity<ServerTransformDeferredResponseDto> e = this.transformClient.getObject().transformAsync(
                sourceType.getValue(), format, sourceCrs, targetCrs, file, EnumTransformResponse.DEFERRED.getValue()
            );


            final ServerTransformDeferredResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch(final TransformServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[Transform Service] Operation has failed", ex);

            throw new TransformServiceException(TransformServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public ServerStatusResponseDto getStatus(String ticket) throws TransformServiceException {
        try {
            final ResponseEntity<ServerStatusResponseDto> e = this.transformClient.getObject().getStatus(ticket);

            final ServerStatusResponseDto serviceResponse = e.getBody();

            return serviceResponse;
        } catch (final Exception ex) {
            logger.error("[Transform Service] Operation has failed", ex);

            throw new TransformServiceException(TransformServiceMessageCode.UNKNOWN);
        }
    }

    @Override
    public void getResource(String ticket, String target) throws TransformServiceException {
        try {
            final File targetFile = new File(target);

            if(targetFile.exists()) {
                throw new TransformServiceException(
                    TransformServiceMessageCode.TARGET_EXISTS,
                    String.format("Target file [%s] already exists", target)
                );
            }


            final Response e = this.transformClient.getObject().getResource(ticket);

            if (e.status() != HttpStatus.OK.value()) {
                TransformServiceMessageCode code    = TransformServiceMessageCode.SERVICE_ERROR;
                String                      details = "-";

                try (final InputStream input = e.body().asInputStream()) {
                    details = IOUtils.toString(input, StandardCharsets.UTF_8);
                } catch (final Exception ex) {
                    details = "<Could not fetch error details>";
                }

                logger.error(
                    "[Transform Service] Operation has failed with code [{}] and reason [{}]. Additional information: {}",
                    e.status(), e.reason(), details
                );

                if (e.status() == HttpStatus.NOT_FOUND.value()) {
                    code = TransformServiceMessageCode.RESOURCE_NOT_FOUND;
                }

                throw new TransformServiceException(code);
            }

            try (final InputStream input = e.body().asInputStream()) {
                FileUtils.copyInputStreamToFile(input, targetFile);
            }
        } catch(final TransformServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[Transform Service] Operation has failed", ex);

            throw new TransformServiceException(TransformServiceMessageCode.UNKNOWN);
        }
    }

}
