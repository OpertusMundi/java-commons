package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.domain.AssetMetadataPropertyEntity;
import eu.opertusmundi.common.domain.RecordLockEntity;
import eu.opertusmundi.common.domain.UserServiceEntity;
import eu.opertusmundi.common.model.EnumLockResult;
import eu.opertusmundi.common.model.EnumRecordLock;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RecordLockDto;
import eu.opertusmundi.common.model.SettingDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.EnumMetadataPropertyType;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.UserServiceMessageCode;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.model.geodata.EnumGeodataWorkspace;
import eu.opertusmundi.common.model.geodata.UserGeodataConfiguration;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AssetMetadataPropertyRepository;
import eu.opertusmundi.common.repository.SettingRepository;
import eu.opertusmundi.common.repository.UserServiceLockRepository;
import eu.opertusmundi.common.repository.UserServiceRepository;
import eu.opertusmundi.common.service.ogc.UserGeodataConfigurationResolver;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.common.util.StreamUtils;

@Service
public class DefaultUserServiceService implements UserServiceService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserServiceService.class);

    private static final String METADATA_PROPERTY_MBR = "mbr";

    private final AssetMetadataPropertyRepository  assetMetadataPropertyRepository;
    private final BpmEngineUtils                   bpmEngine;
    private final ObjectMapper                     objectMapper;
    private final SettingRepository                settingRepository;
    private final UserFileManager                  userFileManager;
    private final UserGeodataConfigurationResolver userGeodataConfigurationResolver;
    private final UserServiceFileManager           userServiceFileManager;
    private final UserServiceLockRepository        userServiceLockRepository;
    private final UserServiceRepository            userServiceRepository;

    @Autowired
    public DefaultUserServiceService(
        AssetMetadataPropertyRepository assetMetadataPropertyRepository,
        BpmEngineUtils bpmEngine,
        ObjectMapper objectMapper,
        SettingRepository settingRepository,
        UserFileManager userFileManager,
        UserGeodataConfigurationResolver userGeodataConfigurationResolver,
        UserServiceLockRepository userServiceLockRepository,
        UserServiceRepository userServiceRepository,
        UserServiceFileManager userServiceFileManager
    ) {
        this.assetMetadataPropertyRepository  = assetMetadataPropertyRepository;
        this.bpmEngine                        = bpmEngine;
        this.objectMapper                     = objectMapper;
        this.settingRepository                = settingRepository;
        this.userFileManager                  = userFileManager;
        this.userGeodataConfigurationResolver = userGeodataConfigurationResolver;
        this.userServiceLockRepository        = userServiceLockRepository;
        this.userServiceRepository            = userServiceRepository;
        this.userServiceFileManager           = userServiceFileManager;
    }

    @Override
    public PageResultDto<UserServiceDto> findAll(
        UUID ownerKey, UUID parentKey,
        Set<EnumUserServiceStatus> status, Set<EnumUserServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumUserServiceSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        if (status != null && status.isEmpty()) {
            status = null;
        }
        if (serviceType != null && serviceType.isEmpty()) {
            serviceType = null;
        }

        final Page<UserServiceEntity> entities = ownerKey == null || ownerKey.equals(parentKey)
            ? this.userServiceRepository.findAllByOwnerAndStatus(parentKey, status, serviceType, pageRequest)
            : this.userServiceRepository.findAllByOwnerAndParentAndStatus(ownerKey, parentKey, status, serviceType, pageRequest);

        final Page<UserServiceDto> items   = entities.map(UserServiceEntity::toDto);
        final long                 count   = items.getTotalElements();
        final List<UserServiceDto> records = items.getContent();

        items.forEach(this::injectProperties);

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    @Transactional
    public UserServiceDto findOne(UUID ownerKey, UUID parentKey, UUID serviceKey) {
        return this.findOne(ownerKey, parentKey, serviceKey, false);
    }

    private UserServiceDto findOne(UUID ownerKey, UUID parentKey, UUID serviceKey, boolean includeHelpdeskDetails) {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(parentKey, "Expected a non-null parent key");
        Assert.notNull(serviceKey, "Expected a non-null service key");

        final UserServiceEntity e = ownerKey.equals(parentKey)
            ? this.userServiceRepository.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null)
            : this.userServiceRepository.findOneByOwnerAndParentAndKey(ownerKey, parentKey, serviceKey).orElse(null);

        final UserServiceDto service = e != null ? e.toDto(true) : null;
        if (service != null) {
            this.injectProperties(service);
        }

        return service;
    }

    @Override
    public UserServiceDto findOne(UUID serviceKey) {
        final UserServiceEntity e = this.userServiceRepository.findOneByKey(serviceKey).orElse(null);

        final UserServiceDto service = e != null ? e.toDto() : null;
        if (service != null) {
            this.injectProperties(service);
        }

        return service;
    }

    @Override
    @Transactional
    public UserServiceDto create(UserServiceCommandDto command) throws UserServiceException {
        try {
            // Resolve resource file
            final FilePathCommand fileCommand = FilePathCommand.builder()
                .path(command.getPath())
                .userName(command.getUserName())
                .build();

            final Path resourcePath = this.userFileManager.resolveFilePath(fileCommand);
            command.setFileName(FilenameUtils.getName(resourcePath.toString()));
            command.setFileSize(resourcePath.toFile().length());

            // Create service
            final UserServiceDto service = this.userServiceRepository.create(command);
            command.setServiceKey(service.getKey());

            // Copy resource from Topio drive
            try (final InputStream input = Files.newInputStream(resourcePath)) {
                this.userServiceFileManager.uploadResource(command, input);
            } catch(final UserServiceException ex) {
                throw ex;
            } catch(final Exception ex) {
                throw new UserServiceException(
                    UserServiceMessageCode.RESOURCE_COPY,
                    String.format("Failed to copy resource file [%s]", command.getPath()), ex
                );
            }

            // Check if workflow exists
            final String       businessKey = service.getKey().toString();
            ProcessInstanceDto instance    = this.bpmEngine.findInstance(businessKey);

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), command.getOwnerKey().toString())
                    .variableAsString("ownerKey", command.getOwnerKey().toString())
                    .variableAsString("ownerName", service.getOwner().getUsername())
                    .variableAsString("parentKey", command.getParentKey().toString())
                    .variableAsString("serviceKey", businessKey)
                    .variableAsString("status", service.getStatus().toString())
                    .variableAsString("serviceTitle", command.getTitle())
                    .variableAsString("serviceVersion", command.getVersion())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(
                    EnumWorkflow.PUBLISH_USER_SERVICE, businessKey, variables, true
                );
            }

            this.userServiceRepository.setWorkflowInstance(service.getKey(), instance.getDefinitionId(), instance.getId());

            return service;
        } catch (final UserServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to cancel asset publication", ex);
        }
    }

    @Override
    @Transactional
    public void publish(UUID ownerKey, UUID parentKey, UUID serviceKey) throws UserServiceException {
        try {
            // Validate service state
            final UserServiceDto service = this.findOne(ownerKey, parentKey, serviceKey);

            if (service == null) {
                throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
            }

            if (service.getStatus() != EnumUserServiceStatus.PROCESSING) {
                throw new UserServiceException(
                    UserServiceMessageCode.INVALID_STATE,
                    String.format("Expected status to be [PROCESSING]. Found [%s]", service.getStatus())
                );
            }

            // Update service status
            this.userServiceRepository.publish(ownerKey, serviceKey);
        } catch (final UserServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public void cancelPublishOperation(
        UUID ownerKey, UUID serviceKey, String errorDetails, List<Message> errorMessages
    ) throws UserServiceException {
        try {
            final UserServiceEntity service = this.userServiceRepository.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

            if (service == null) {
                throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
            }
            // Cleanup files
            this.userServiceFileManager.reset(ownerKey, serviceKey);
            // Update data
            this.userServiceRepository.reset(ownerKey, serviceKey, errorDetails, errorMessages);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to cancel asset publication", ex);
        }
    }

    @Override
    @Transactional
    public void updateMetadata(
        UUID parentKey, UUID serviceKey, JsonNode value
    ) throws FileSystemException, UserServiceException {
        try {
            // The provider must have access to the selected service and also the
            // service must be already accepted by the HelpDesk. Since metadata is
            // updated by the publish workflow, we assume that owner key is equal to
            // parent key.
            final UserServiceDto service = this.ensureServiceAndStatus(parentKey, parentKey, serviceKey, EnumUserServiceStatus.PROCESSING);

            // Store all metadata in asset repository
            final String content = objectMapper.writeValueAsString(value);

            this.userServiceFileManager.saveMetadataAsText(parentKey, serviceKey, serviceKey + ".automated-metadata.json", content);

            // Filter properties before updating metadata in catalogue service
            final List<AssetMetadataPropertyEntity> properties = this.assetMetadataPropertyRepository.findAllByAssetType(EnumAssetType.VECTOR);

            for(final AssetMetadataPropertyEntity p: properties) {
                final String   propertyName = p.getName();
                final JsonNode propertyNode = value.get(propertyName);

                // Ignore undefined or null nodes
                if (propertyNode == null || propertyNode.isNull()) {
                    continue;
                }

                final String fileName = this.getMetadataPropertyFileName(serviceKey, propertyName, p.getType());

                switch (p.getType()) {
                    case PNG :
                        this.userServiceFileManager.saveMetadataPropertyAsImage(parentKey, serviceKey, fileName, propertyNode.asText());
                        break;
                    case JSON :
                        this.userServiceFileManager.saveMetadataPropertyAsJson(parentKey, serviceKey, fileName, objectMapper.writeValueAsString(propertyNode));
                        break;
                }

                final String uri = String.format(
                    "/action/user/services/%s/metadata/%s",
                    serviceKey, propertyName
                );

                ((ObjectNode) value).put(propertyName, uri);
            }

            // Store filtered metadata in asset repository
            final String filteredContent = objectMapper.writeValueAsString(value);

            this.userServiceFileManager.saveMetadataAsText(parentKey, serviceKey, serviceKey + ".automated-metadata-minified.json", filteredContent);

            // Set resource key in metadata
            ((ObjectNode) value).put("key", serviceKey.toString());

            // Set geometry, if not already set, from metadata
            Geometry geometry = null;
            if (service.getGeometry() == null) {
                final JsonNode mbrNode = value.get(METADATA_PROPERTY_MBR);

                // Ignore undefined or null nodes
                if (mbrNode != null && !mbrNode.isNull()) {
                    final WKTReader mbrReader = new WKTReader();
                    geometry = mbrReader.read(mbrNode.asText());
                }
            }

            this.userServiceRepository.updateMetadataAndGeometry(parentKey, serviceKey, value, geometry);
        } catch (final UserServiceException ex) {
            throw ex;
        } catch (final JsonProcessingException ex) {
            throw new UserServiceException(
                UserServiceMessageCode.METADATA_SERIALIZATION,
                String.format("Failed to serialize automated metadata for asset [%s]", serviceKey), ex
            );
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public void updateResourceIngestionData(
        UUID parentKey, UUID serviceKey, ServerIngestResultResponseDto data
    ) throws UserServiceException {
        try {
            // The provider must have access to the selected service and also the
            // service must be already accepted by the provider. Since ingestion
            // data is updated by the workflow, we assume that the owner key is
            // equal to the parent key
            this.ensureServiceAndStatus(parentKey, parentKey, serviceKey, EnumUserServiceStatus.PROCESSING);

            this.userServiceRepository.updateResourceIngestionData(
                parentKey, serviceKey,ResourceIngestionDataDto.from(serviceKey.toString(), data)
            );
        } catch (final UserServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public void updateResourceIngestionData(
        UUID parentKey, UUID serviceKey, ServerIngestPublishResponseDto data
    ) throws UserServiceException {
        try {
            // The provider must have access to the selected service and also the
            // service must be already accepted by the provider. Since ingestion
            // data is updated by the workflow, we assume that the owner key is
            // equal to the parent key
            this.ensureServiceAndStatus(parentKey, parentKey, serviceKey, EnumUserServiceStatus.PROCESSING);

            this.userServiceRepository.updateResourceIngestionData(parentKey, serviceKey, data);
        } catch (final UserServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new UserServiceException(UserServiceMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public MetadataProperty resolveMetadataProperty(
        UUID ownerKey, UUID parentKey, UUID serviceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException {
        // The provider must have access to the selected service
        this.ensureServiceAndStatus(ownerKey, parentKey, serviceKey, EnumUserServiceStatus.PUBLISHED);

        final AssetMetadataPropertyEntity property = this.assetMetadataPropertyRepository
            .findOneByAssetTypeAndName(EnumAssetType.VECTOR, propertyName)
            .orElse(null);

        if (property != null) {
            final String fileName = this.getMetadataPropertyFileName(serviceKey, propertyName, property.getType());
            final Path   path     = this.userServiceFileManager.resolveMetadataPropertyPath(ownerKey, serviceKey, fileName);

            if (path.toFile().exists()) {
                return MetadataProperty.of(property.getType(), path);
            }
        }

        throw new FileSystemException(FileSystemMessageCode.FILE_IS_MISSING, "File not found");
    }

    @Override
    @Transactional
    public void delete(UUID ownerKey, UUID parentKey, UUID serviceKey) throws UserServiceException {
        try {
            final UserServiceDto service = this.ensureServiceAndStatus(
                ownerKey, parentKey, serviceKey, EnumUserServiceStatus.FAILED, EnumUserServiceStatus.PUBLISHED
            );

            this.getLock(ownerKey, serviceKey, true);

            // Remove database entry and local files
            this.userServiceRepository.remove(ownerKey, serviceKey);
            this.userServiceFileManager.deleteAllFiles(ownerKey, serviceKey);

            switch (service.getStatus()) {
                case PUBLISHED : {
                    // For published services, a workflow instance must be
                    // created
                    final String       businessKey = service.getKey().toString() + "::remove";
                    ProcessInstanceDto instance    = this.bpmEngine.findInstance(businessKey);

                    if (instance == null) {
                        final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                            .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), ownerKey.toString())
                            .variableAsString("ownerKey", ownerKey.toString())
                            .variableAsString("ownerName", service.getOwner().getUsername())
                            .variableAsString("parentKey", parentKey.toString())
                            .variableAsUuid("serviceKey", serviceKey)
                            .variableAsString("serviceTitle", service.getTitle())
                            .variableAsString("serviceVersion", service.getVersion())
                            .build();

                        instance = this.bpmEngine.startProcessDefinitionByKey(
                            EnumWorkflow.REMOVE_USER_SERVICE, businessKey, variables, true
                        );
                    }
                    break;
                }

                default :
                    // No action required
            }
        } finally {
            this.releaseLock(ownerKey, serviceKey);
        }
    }

    @Override
    @Transactional
    public void releaseLock(UUID userKey, UUID serviceKey) throws UserServiceException {
        final Optional<RecordLockEntity> lock = this.userServiceLockRepository.findOne(serviceKey);

        if (lock.isPresent()) {
            if (!lock.get().getOwner().getKey().equals(userKey)) {
                throw new UserServiceException(
                    UserServiceMessageCode.LOCK_EXISTS,
                    String.format("Record belongs to user [%s]", lock.get().getOwner().getEmail())
                );
            }

            userServiceLockRepository.delete(lock.get());
        }
    }

    private UserServiceDto ensureServiceAndStatus(
        UUID ownerKey, UUID parentKey, UUID assetKey, EnumUserServiceStatus... status
    ) throws UserServiceException {
        final UserServiceDto service = this.findOne(ownerKey, parentKey, assetKey, true);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        if (status != null && !ArrayUtils.contains(status, service.getStatus())) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", StringUtils.join(status, ","), service.getStatus())
            );
        }

        return service;
    }

    private String getMetadataPropertyFileName(UUID serviceKey, String propertyName, EnumMetadataPropertyType propertyType) {
        return StringUtils.joinWith(".", serviceKey, "property", propertyName, propertyType.getExtension());
    }

    private Pair<EnumLockResult, RecordLockDto> getLock(UUID userKey, UUID serviceKey, boolean required) throws UserServiceException {
        final RecordLockDto lock = this.userServiceLockRepository.findOneObject(serviceKey).orElse(null);

        if (required && lock != null && !lock.getOwnerKey().equals(userKey)) {
            throw new UserServiceException(
                UserServiceMessageCode.LOCK_EXISTS,
                String.format("Record is already locked by user [%s]", lock.getOwnerEmail())
            );
        }
        if (required && lock == null) {
            final Integer       recordId = this.userServiceRepository.getIdFromKey(serviceKey);
            final RecordLockDto newLock  = this.userServiceLockRepository.create(EnumRecordLock.USER_SERVICE, recordId, userKey);

            return Pair.of(EnumLockResult.CREATED, newLock);
        }

        return Pair.of(lock == null ? EnumLockResult.NONE : EnumLockResult.EXISTS, lock);
    }

    private void injectProperties(UserServiceDto service) {
        final PerCallPricingModelCommandDto pricingModel = new PerCallPricingModelCommandDto();
        final SettingDto                    pricePerCall = this.settingRepository.findOne(EnumSetting.USER_SERVICE_PRICE_PER_CALL);

        pricingModel.setPrice(new BigDecimal(pricePerCall.getValue()));

        service.setPricingModel(pricingModel);
        this.updateIngestionData(service);

    }

    private void updateIngestionData(UserServiceDto service) {
        final var geodataConfig = this.userGeodataConfigurationResolver.resolveFromUserKey(service.getOwner().getKey(), EnumGeodataWorkspace.PRIVATE);

        service.getIngestData().getEndpoints().stream().forEach(e -> {
            e.setUri(this.getEndpointAbsoluteUrl(geodataConfig, e.getType(), e.getUri()));
        });

        final var effectiveEndpoints = StreamUtils.from(service.getIngestData().getEndpoints())
            .filter(e -> service.getServiceType().getAllowedOgcServiceTypes().contains(e.getType()))
            .toList();

        service.getIngestData().setEndpoints(effectiveEndpoints);
    }

    private String getEndpointAbsoluteUrl(UserGeodataConfiguration config, EnumSpatialDataServiceType type, String endpoint) {
        return switch (type) {
            case WMS -> config.getWmsEndpoint() + "?" + endpoint.split("\\?")[1];
            case WFS -> config.getWfsEndpoint() + "?" + endpoint.split("\\?")[1];
            default -> null;
        };
    }
}
