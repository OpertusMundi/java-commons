package eu.opertusmundi.common.service;

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ConsumerServiceException;
import eu.opertusmundi.common.model.account.ConsumerServiceMessageCode;
import eu.opertusmundi.common.model.account.EnumSubscriptionStatus;
import eu.opertusmundi.common.model.asset.EnumConsumerAssetSortField;
import eu.opertusmundi.common.model.asset.EnumConsumerSubSortField;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.file.CopyToDriveCommandDto;
import eu.opertusmundi.common.model.file.CopyToDriveResultDto;
import eu.opertusmundi.common.model.file.FileCopyResourceCommandDto;
import eu.opertusmundi.common.model.file.FileCopyResourceDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountAssetRepository;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.AssetStatisticsRepository;
import eu.opertusmundi.common.repository.FileCopyResourceRepository;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;

// TODO: Implement data filtering/sorting/pagination at the database level

@Service
public class DefaultConsumerAssetService implements ConsumerAssetService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerAssetService.class);

    @Value("${opertusmundi.file-system.async-copy-threshold-size:10485760}")
    private long asyncCopyThresholdSize;

    private final AccountRepository             accountRepository;
    private final AccountAssetRepository        accountAssetRepository;
    private final AccountSubscriptionRepository accountSubscriptionRepository;
    private final AssetFileManager              assetFileManager;
    private final AssetStatisticsRepository     assetStatisticsRepository;
    private final BpmEngineUtils                bpmEngine;
    private final CatalogueService              catalogueService;
    private final FileCopyResourceRepository    fileCopyResourceRepository;
    private final UserFileManager               userFileManager;

    @Autowired
    public DefaultConsumerAssetService(
        AccountRepository accountRepository,
        AccountAssetRepository accountAssetRepository,
        AccountSubscriptionRepository accountSubscriptionRepository,
        AssetStatisticsRepository assetStatisticsRepository,
        AssetFileManager assetFileManager,
        BpmEngineUtils bpmEngine,
        CatalogueService catalogueService,
        FileCopyResourceRepository fileCopyResourceRepository,
        UserFileManager userFileManager
    ) {
        this.accountRepository             = accountRepository;
        this.accountAssetRepository        = accountAssetRepository;
        this.accountSubscriptionRepository = accountSubscriptionRepository;
        this.assetStatisticsRepository     = assetStatisticsRepository;
        this.assetFileManager              = assetFileManager;
        this.bpmEngine                     = bpmEngine;
        this.catalogueService              = catalogueService;
        this.fileCopyResourceRepository    = fileCopyResourceRepository;
        this.userFileManager               = userFileManager;
    }

    @Override
    public PageResultDto<AccountAssetDto> findAllAssets(
            UUID userKey, EnumAssetType type, int pageIndex, int pageSize, EnumConsumerAssetSortField orderBy, EnumSortingOrder order
    ) {
        List<AccountAssetDto> records = this.accountAssetRepository.findAllByUserKey(userKey).stream()
            .map(e -> e.toDto())
            .collect(Collectors.toList());

        if (records.isEmpty()) {
            return PageResultDto.empty(PageRequestDto.of(pageIndex, pageSize));
        }

        final String[]                      pid    = records.stream().map(a -> a.getAssetId()).distinct().toArray(String[]::new);
        final List<CatalogueItemDetailsDto> assets = this.catalogueService.findAllById(pid);

        // Add catalogue items to records
        records.forEach(r -> {
            final CatalogueItemDto item = assets.stream()
                .filter(a -> a.getId().equals(r.getAssetId()))
                .findFirst()
                .orElse(null);

            if(item == null) {
                throw new ConsumerServiceException(ConsumerServiceMessageCode.CATALOGUE_ITEM_NOT_FOUND, String.format(
                    "Catalogue item not found [userKey=%s, accountAsset=%d, assetPid=%s]" ,
                    userKey, r.getId(), r.getAssetId()
                ));
            }
            // Remove superfluous data
            item.setAutomatedMetadata(null);

            r.setItem(item);
        });

        // Filtering
        if (type != null) {
            records = records.stream().filter(r -> r.getItem().getType() == type).collect(Collectors.toList());
        }
        // Sorting
        records.sort((r1, r2) -> {
            switch(orderBy) {
                case ADDED_ON:
                    return r1.getAddedOn().compareTo(r2.getAddedOn());
                case PUBLISHER:
                    // TODO: Check if the publisher name is in sync with
                    // platform providers
                    return r1.getItem().getPublisherName().compareTo(r2.getItem().getPublisherName());
                case TITLE:
                    return r1.getItem().getTitle().compareTo(r2.getItem().getTitle());
                case UPDATE_ELIGIBILITY:
                    return r1.getUpdateEligibility().compareTo(r2.getUpdateEligibility());
            }
            return 0;
        });
        // Pagination
        final List<AccountAssetDto> items = records.stream()
            .skip(pageIndex * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, items);
    }

    @Override
    public PageResultDto<AccountSubscriptionDto> findAllSubscriptions(
            UUID userKey, EnumSpatialDataServiceType type, int pageIndex, int pageSize, EnumConsumerSubSortField orderBy, EnumSortingOrder order
    ) {
        List<AccountSubscriptionDto> records = this.accountSubscriptionRepository.findAllObjectsByConsumer(userKey, false);

        if (records.isEmpty()) {
            return PageResultDto.empty(PageRequestDto.of(pageIndex, pageSize));
        }

        final String[]               pid    = records.stream().map(a -> a.getAssetId()).distinct().toArray(String[]::new);
        final List<CatalogueItemDetailsDto> assets = this.catalogueService.findAllById(pid);

        // Add catalogue items to records
        records.forEach(r -> {
            final CatalogueItemDto item = assets.stream()
                .filter(a -> a.getId().equals(r.getAssetId()))
                .findFirst()
                .orElse(null);

            if(item == null) {
                throw new ConsumerServiceException(ConsumerServiceMessageCode.CATALOGUE_ITEM_NOT_FOUND, String.format(
                    "Catalogue item not found [userKey=%s, accountAsset=%d, assetPid=%s]" ,
                    userKey, r.getId(), r.getAssetId()
                ));
            }
            // Remove superfluous data
            item.setAutomatedMetadata(null);

            r.setItem(item);
        });

        // Filtering
        if (type != null) {
            records = records.stream().filter(r -> r.getItem().getSpatialDataServiceType() == type).collect(Collectors.toList());
        }
        // Sorting
        records.sort((r1, r2) -> {
            switch(orderBy) {
                case ADDED_ON:
                    return r1.getAddedOn().compareTo(r2.getAddedOn());
                case UPDATED_ON:
                    return r1.getUpdatedOn().compareTo(r2.getUpdatedOn());
                case PUBLISHER:
                    // TODO: Check if the publisher name is in sync with
                    // platform providers
                    return r1.getItem().getPublisherName().compareTo(r2.getItem().getPublisherName());
                case TITLE:
                    return r1.getItem().getTitle().compareTo(r2.getItem().getTitle());
            }
            return 0;
        });
        // Pagination
        final List<AccountSubscriptionDto> items = records.stream()
            .skip(pageIndex * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, items);
    }

    @Override
    public AccountSubscriptionDto findSubscription(UUID userKey, UUID subscriptionKey) {
       final AccountSubscriptionDto result = this.accountSubscriptionRepository.findOneObjectByConsumerAndOrder(userKey, subscriptionKey, true)
           .orElse(null);

        if (result == null) {
            return result;
        }

        final List<CatalogueItemDetailsDto> assets = this.catalogueService.findAllById(new String[]{result.getAssetId()});

        if(assets.isEmpty()) {
            throw new ConsumerServiceException(ConsumerServiceMessageCode.CATALOGUE_ITEM_NOT_FOUND, String.format(
                "Catalogue item not found [userKey=%s, assetPid=%s]" ,
                userKey,result.getAssetId()
            ));
        }
        result.setItem(assets.get(0));

       return result;
    }

    @Override
    @Transactional
    public void cancelSubscription(UUID userKey, UUID subscriptionKey) {
        final AccountSubscriptionEntity subscription = this.accountSubscriptionRepository
            .findOneByConsumerAndOrder(userKey, subscriptionKey)
            .orElse(null);

        if (subscription == null || subscription.getStatus() == EnumSubscriptionStatus.INACTIVE) {
            return;
        }
        subscription.setStatus(EnumSubscriptionStatus.INACTIVE);
        subscription.setCancelledOn(ZonedDateTime.now());

        this.accountSubscriptionRepository.saveAndFlush(subscription);
    }

    @Override
    @Transactional
    public FileResourceDto resolveResourcePath(UUID userKey, String pid, String resourceKey) throws ServiceException {
        // Check asset ownership
        final boolean owned = this.accountAssetRepository.checkOwnershipByAsset(userKey, pid);
        if (!owned) {
            logger.warn(
                "User attempt to download resource of not purchased asset. [userKey={}, pid={}, resourceKey={}]",
                userKey, pid, resourceKey
            );
            throw ServiceException.unauthorized();
        }
        // Check asset
        final CatalogueItemDetailsDto asset = this.catalogueService.findOne(null, pid, null, false);
        if (asset == null) {
            logger.warn(
                "User attempt to download resource of non-existent asset. [userKey={}, pid={}, resourceKey={}]",
                userKey, pid, resourceKey
            );
            throw ServiceException.notFound();
        }
        // Check if asset allows resource downloading
        if (!asset.getType().isResourceDownloadAllowed()) {
            logger.warn(
                "Asset type does not support resource download. [userKey={}, pid={}, resourceKey={}, type={}]",
                userKey, pid, resourceKey, asset.getType()
            );
            throw ServiceException.notFound();
        }

        // Check resource
        final ResourceDto resource = asset.getResources().stream().filter(r -> r.getId().equals(resourceKey)).findFirst().orElse(null);
        if (resource == null || resource.getType() != EnumResourceType.FILE) {
            throw ServiceException.notFound();
        }

        try {
            final FileResourceDto fileResource = (FileResourceDto) resource;
            final Path            path         = assetFileManager.resolveResourcePath(pid, fileResource.getFileName());
            fileResource.setAsset(asset);
            fileResource.setRelativePath(path);
            fileResource.setPath(path.toString());

            assetStatisticsRepository.increaseDownloads(pid);

            return fileResource;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to resolve resource path. [asset=%s, resourceKey=%s]", pid, resourceKey), ex);
            throw ServiceException.error("Failed to resolve resource path");
        }
    }

    @Override
    @Transactional
    public CopyToDriveResultDto copyToDrive(CopyToDriveCommandDto command) throws ServiceException {
        Assert.notNull(command, "Expected a non-null command");

        final UUID   userKey      = command.getUserKey();
        final String pid          = command.getPid();
        final String resourceKey  = command.getResourceKey();
        final String relativePath = command.getPath();

        Assert.notNull(userKey, "Expected a non-null user key");
        Assert.hasText(pid, "Expected a non-empty asset PID");
        Assert.hasText(resourceKey, "Expected a non-empty resource key");
        Assert.hasText(relativePath, "Expected a non-empty relative path");

        try {
            final FileResourceDto resource = this.resolveResourcePath(userKey, pid, resourceKey);
            final AccountDto      account  = this.accountRepository.findOneByKeyObject(userKey).orElse(null);
            final String          userName = account.getEmail();

            final FilePathCommand pathCommand = FilePathCommand.builder()
                .path(relativePath)
                .userName(userName)
                .build();

            final Path   targetDir  = this.userFileManager.resolveDirPath(pathCommand);
            final String fileName   = StringUtils.isBlank(command.getFileName()) ? resource.getFileName() : command.getFileName();
            final Path   sourcePath = resource.getRelativePath();
            final Path   targetPath = targetDir.resolve(fileName);

            if (resource.getSize() < asyncCopyThresholdSize) {
                // Perform synchronous copy operation
                FileUtils.copyFile(sourcePath.toFile(), targetPath.toFile(), true, StandardCopyOption.REPLACE_EXISTING);
                assetStatisticsRepository.increaseDownloads(pid);

                return CopyToDriveResultDto.of(false);
            }

            // Start workflow instance for asynchronous copy operation
            this.startCopyResourceToDriveWorkflow(userKey, pid, resourceKey, sourcePath, targetPath, resource);

            return CopyToDriveResultDto.of(true);
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format(
                "File copy operation has failed. [userKey=%s, asset=%s, resourceKey=%s, relativePath=%s]",
                userKey, pid, resourceKey, relativePath
            ), ex);
            throw new ServiceException(BasicMessageCode.IOError, "File copy operation has failed", ex);
        }
    }

    private void startCopyResourceToDriveWorkflow(
        UUID userKey, String pid, String resourceKey, Path sourcePath, Path targetPath, FileResourceDto resource
    ) {
        final EnumWorkflow workflow = EnumWorkflow.CONSUMER_COPY_RESOURCE_TO_DRIVE;

        try {
            final FileCopyResourceCommandDto command = FileCopyResourceCommandDto.builder()
                .accountKey(userKey)
                .assetPid(pid)
                .resourceKey(resourceKey)
                .sourcePath(sourcePath.toString())
                .targetPath(targetPath.toString())
                .size(sourcePath.toFile().length())
                .build();

            final FileCopyResourceDto fileCopy = this.fileCopyResourceRepository.create(command);

            // Set business key
            final String businessKey= fileCopy.getIdempotentKey().toString();

            // Set variables
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey.toString())
                .variableAsString("userKey", userKey.toString())
                .variableAsString("assetId", pid)
                .variableAsString("assetName", resource.getAsset().getTitle())
                .variableAsString("assetVersion", resource.getAsset().getVersion())
                .variableAsString("resourceKey", resourceKey)
                .variableAsString("resourceFileName", resource.getFileName())
                .variableAsString("sourcePath", sourcePath.toString())
                .variableAsString("targetPath", targetPath.toString())
                .build();

            this.bpmEngine.startProcessDefinitionByKey(workflow, businessKey, variables);
        } catch(final Exception ex) {
            logger.error(String.format(
                "Failed to start workflow instance [workflow=%s, userKey=%s, pid=%s, resourceKey=%s]",
                workflow, userKey, pid,resourceKey
            ), ex);

            throw new ServiceException(BasicMessageCode.InternalServerError, "Resource file copy operation has failed", ex);
        }
    }

}
