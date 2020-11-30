package eu.opertusmundi.common.service;

import java.util.Set;
import java.util.UUID;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;

public interface ProviderAssetService {

    /**
     * Search drafts
     *
     * @param publisherKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<AssetDraftDto> findAllDraft(
        UUID publisherKey, Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize, String orderBy, String order
    );

    default PageResultDto<AssetDraftDto> findAllDraft(
        UUID publisherKey, Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize
    ) {
        return this.findAllDraft(publisherKey, status, pageIndex, pageSize, "modifiedOn", "desc");
    }

    /**
     * Get one draft by key
     *
     * @param publisherKey
     * @param draftKey
     * @return
     */
    AssetDraftDto findOneDraft(UUID publisherKey, UUID draftKey);

    /**
     * Update a draft
     *
     * The status must be {@link EnumProviderAssetDraftStatus#DRAFT}
     *
     * @param draft
     * @return
     */
    AssetDraftDto updateDraft(CatalogueItemCommandDto command) throws AssetDraftException;

    /**
     * Delete a draft
     *
     * The status must be one of:
     * {@link EnumProviderAssetDraftStatus#DRAFT}
     * {@link EnumProviderAssetDraftStatus#HELPDESK_REJECTED}
     * {@link EnumProviderAssetDraftStatus#PROVIDER_REJECTED}
     *
     * @param publisheKey
     * @param draftKey
     */
    void deleteDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException;

    /**
     * Submit a draft to OP HelpDesk for review
     *
     * The status must be {@link EnumProviderAssetDraftStatus#DRAFT}
     *
     * @param command
     * @return
     */
    void submitDraft(CatalogueItemCommandDto command) throws AssetDraftException;

    /**
     * Update draft status
     *
     * @param command
     * @throws AssetDraftException
     */
    void updateStatus(AssetDraftSetStatusCommandDto command) throws AssetDraftException;

    /**
     * Accept a draft from a HelpDesk account
     *
     * @param publisherKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void acceptHelpDesk(UUID publisherKey, UUID draftKey) throws AssetDraftException;

    /**
     * Reject a draft from a HelpDesk account
     *
     * @param publisherKey
     * @param draftKey
     * @param reason
     * @throws AssetDraftException
     */
    void rejectHelpDesk(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException;

    /**
     * Accept a draft from a provider account
     *
     * @param command
     * @throws AssetDraftException
     */
    void acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Reject a draft from a provider account
     *
     * @param command
     * @throws AssetDraftException
     */
    void rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Publish draft to catalogue service
     *
     * @param publisherKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void publishDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException;

}
