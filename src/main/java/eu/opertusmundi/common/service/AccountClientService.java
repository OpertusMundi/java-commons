package eu.opertusmundi.common.service;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountClientCommandDto;
import eu.opertusmundi.common.model.account.AccountClientDto;
import eu.opertusmundi.common.model.account.EnumAccountClientStatus;

public interface AccountClientService {

    default PageResultDto<AccountClientDto> findAll(UUID key) {
        return this.findAll(key, 0, 10, EnumAccountClientStatus.ALL);
    }

    default PageResultDto<AccountClientDto> findAll(UUID key, int page, int size, EnumAccountClientStatus status) {
        final Direction   direction   = Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, "alias"));

        return this.findAll(key, status, pageRequest);
    }

    /**
     * Find all clients for the specified account key
     *
     * @param accountKey
     * @param pageable
     * @return
     */
    default PageResultDto<AccountClientDto> findAll(UUID accountKey, Pageable pageable) {
        return this.findAll(accountKey, EnumAccountClientStatus.ALL, pageable);
    }

    /**
     * Find all clients for the specified account key
     *
     * @param accountKey
     * @param status
     * @param pageable
     * @return
     */
    PageResultDto<AccountClientDto> findAll(UUID accountKey, EnumAccountClientStatus status, Pageable pageable);

    /**
     * Create new client and return the new client with its secret
     *
     * @param command
     * @return
     * @throws ServiceException
     */
    AccountClientDto create(AccountClientCommandDto command) throws ServiceException;

    /**
     * Revoke existing client
     *
     * @param accountId
     * @param clientKey
     * @throws ServiceException
     */
    void revoke(Integer accountId, UUID clientKey) throws ServiceException;

}
