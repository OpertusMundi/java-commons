package eu.opertusmundi.common.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountClientEntity;
import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.account.AccountClientDto;

@Repository
@Transactional(readOnly = true)
public interface AccountClientRepository extends JpaRepository<AccountClientEntity, Integer> {

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.id = :id")
    Optional<AccountEntity> findOneAccountById(Integer id);

    @Query("SELECT c FROM AccountClient c WHERE c.key = :key")
    Optional<AccountClientEntity> findOneByKey(String key);

    @Query("SELECT c FROM AccountClient c WHERE c.account.id = :accountId and c.key = :clientKey")
    Optional<AccountClientEntity> findOneByAccountIdAndKey(Integer accountId, UUID clientKey);

    @Query("SELECT c FROM AccountClient c WHERE c.account.key = :accountKey and c.key = :clientKey")
    Optional<AccountClientEntity> findOneByAccountKeyAndKey(UUID accountKey, UUID clientKey);

    @Query("SELECT c FROM AccountClient c WHERE c.account.id = :accountId and c.alias = :alias")
    Optional<AccountClientEntity> findOneByAccountIdAndAlias(Integer accountId, String alias);

    @Query("SELECT c FROM AccountClient c WHERE c.account.key = :accountKey and c.alias = :alias")
    Optional<AccountClientEntity> findOneByAccountKeyAndAlias(UUID accountKey, String alias);

    @Query("SELECT c FROM AccountClient c WHERE c.account.id = :accountId")
    Page<AccountClientEntity> findAllByAccountId(Integer accountId, Pageable pageable);

    @Query("SELECT c FROM AccountClient c WHERE c.account.key = :key")
    Page<AccountClientEntity> findAllByAccountKey(UUID key, Pageable pageable);

    @Transactional(readOnly = false)
    default AccountClientDto create(Integer accountId, String clientAlias, UUID clientKey) {
        Assert.notNull(accountId, "Expected a non-null account identifier");
        Assert.hasText(clientAlias, "Expected a non-empty client alias");
        Assert.notNull(clientKey, "Expected a non-null client key");

        // Check account
        final AccountEntity account = this.findOneAccountById(accountId).orElse(null);
        Assert.notNull(account, "Expected a non-null account");

        // Create new client
        final AccountClientEntity entity = new AccountClientEntity(clientAlias, clientKey);
        entity.setAccount(account);

        return this.saveAndFlush(entity).toDto();
    }

    @Transactional(readOnly = false)
    default AccountClientDto revoke(Integer accountId, UUID clientKey) {
        Assert.notNull(accountId, "Expected a non-null account identifier");
        Assert.notNull(clientKey, "Expected a non-null client key");

        final AccountClientEntity entity = this.findOneByAccountIdAndKey(accountId, clientKey).orElse(null);
        Assert.notNull(entity, "Expected a non-null client");

        entity.revoke();

        return this.saveAndFlush(entity).toDto();
    }

}
