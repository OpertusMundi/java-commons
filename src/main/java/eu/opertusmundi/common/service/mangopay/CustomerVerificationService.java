package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.kyc.CustomerVerificationException;
import eu.opertusmundi.common.model.kyc.KycDocumentCommand;
import eu.opertusmundi.common.model.kyc.KycDocumentCommandDto;
import eu.opertusmundi.common.model.kyc.KycDocumentDto;
import eu.opertusmundi.common.model.kyc.KycDocumentPageCommandDto;
import eu.opertusmundi.common.model.kyc.KycQueryCommand;
import eu.opertusmundi.common.model.kyc.UboCommandDto;
import eu.opertusmundi.common.model.kyc.UboDeclarationCommand;
import eu.opertusmundi.common.model.kyc.UboDeclarationDto;
import eu.opertusmundi.common.model.kyc.UboDto;
import eu.opertusmundi.common.model.kyc.UboQueryCommand;
import eu.opertusmundi.common.model.kyc.UpdateKycLevelCommand;

public interface CustomerVerificationService {

    /**
     * Find all KYC documents
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    PageResultDto<KycDocumentDto> findAllKycDocuments(KycQueryCommand command) throws CustomerVerificationException;

    /**
     * Get KYC document
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    KycDocumentDto findOneKycDocument(KycDocumentCommand command) throws CustomerVerificationException;

    /**
     * Create KYC document
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    KycDocumentDto createKycDocument(KycDocumentCommandDto command) throws CustomerVerificationException;

    /**
     * Add page to KYC document
     *
     * @param command
     * @param data
     * @throws CustomerVerificationException
     */
    void addPage(KycDocumentPageCommandDto command, byte[] data) throws CustomerVerificationException;


    /**
     * Submit KYC document
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    KycDocumentDto submitKycDocument(KycDocumentCommand command) throws CustomerVerificationException;

    /**
     * Find all UBO declarations
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    PageResultDto<UboDeclarationDto> findAllUboDeclarations(UboQueryCommand command) throws CustomerVerificationException;

    /**
     * Get UBO declaration
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDeclarationDto findOneUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException;

    /**
     * Create UBO declaration
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDeclarationDto createUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException;

    /**
     * Add UBO
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDto addUbo(UboCommandDto command) throws CustomerVerificationException;

    /**
     * Update UBO
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDto updateUbo(UboCommandDto command) throws CustomerVerificationException;

    /**
     * Remove UBO
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDto removeUbo(UboCommandDto command) throws CustomerVerificationException;

    /**
     * Submit UBO declaration
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    UboDeclarationDto submitUboDeclaration(UboDeclarationCommand command) throws CustomerVerificationException;

    /**
     * Refresh the KYC level of the consumer/provider MANGOPAY users for
     * the specified account
     *
     * @param accountKey
     * @return
     * @throws CustomerVerificationException
     */
    AccountDto refreshCustomerKycLevel(UUID accountKey) throws CustomerVerificationException;

    /**
     * Update customer KYC level
     *
     * @param command
     * @return
     * @throws CustomerVerificationException
     */
    CustomerDto updateCustomerKycLevel(UpdateKycLevelCommand command) throws CustomerVerificationException;

}
