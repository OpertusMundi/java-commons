package eu.opertusmundi.common.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.AccountClientEntity;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountClientCommandDto;
import eu.opertusmundi.common.model.account.AccountClientDto;
import eu.opertusmundi.common.model.account.AccountMessageCode;
import eu.opertusmundi.common.repository.AccountClientRepository;

@Service
public class DefaultAccountClientService implements AccountClientService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAccountClientService.class);

    private final AccountClientRepository accountClientRepository;

    @Autowired(required = false)
    private final KeycloakAdminService keycloakAdminService;

    @Autowired
    public DefaultAccountClientService(
        AccountClientRepository accountClientRepository,
        Optional<KeycloakAdminService> keycloakAdminService
    ) {
        this.accountClientRepository = accountClientRepository;
        this.keycloakAdminService    = keycloakAdminService.orElse(null);
    }

    @Override
    public PageResultDto<AccountClientDto> findAll(UUID accontKey, Pageable pageable) {
        final Page<AccountClientDto> page = this.accountClientRepository.findAllByAccountKey(accontKey, pageable)
            .map(AccountClientEntity::toDto);

        return PageResultDto.of(pageable.getPageNumber(), pageable.getPageSize(), page.getContent(), page.getTotalElements());
    }

    @Override
    public AccountClientDto create(AccountClientCommandDto command) throws ServiceException {
        try {
            // Check alias uniqueness
            if (this.accountClientRepository.findOneByAccountIdAndAlias(command.getAccountId(), command.getAlias()).isPresent()) {
                throw new ServiceException(AccountMessageCode.ACCOUNT_CLIENT_NOT_UNIQUE_ALIAS, "Client with the same alias already exists");
            }

            // TODO: Create remote (Keycloak) client and get client id (key) and secret
            final UUID   clientKey    = UUID.randomUUID();
            final String clientSecret = "credentials";

            if (keycloakAdminService != null) {
                // ...
            }

            // Create local account client
            final AccountClientDto result = this.accountClientRepository.create(command.getAccountId(), command.getAlias(), clientKey);
            result.setSecret(clientSecret);

            return result;
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = String.format(
                "Failed to create account client. [accountId=%d, clientAlias=%s]",
                command.getAccountId(), command.getAlias()
            );

            logger.error(message, ex);

            throw new ServiceException(AccountMessageCode.ACCOUNT_CLIENT_ERROR, "Failed to create new account client");
        }
    }

    @Override
    public void revoke(Integer accountId, UUID clientKey) throws ServiceException {
        try {
            final AccountClientEntity e = this.accountClientRepository.findOneByAccountIdAndKey(accountId, clientKey).orElse(null);
            if (e == null) {
                throw new ServiceException(AccountMessageCode.ACCOUNT_CLIENT_NOT_FOUND, "Client was not found");
            }

            // Revoke local client
            this.accountClientRepository.revoke(accountId, clientKey);

            // TODO: Revoke remote (Keycloak) client
            if (keycloakAdminService != null) {
                //...
            }
        } catch (final ServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            final String message = String.format("Failed to revoke account client. [accountId=%d, clientKey=%s]", accountId, clientKey);

            logger.error(message,  ex);

            throw new ServiceException(AccountMessageCode.ACCOUNT_CLIENT_ERROR, "Failed to revoke existing account client");
        }
    }

}
