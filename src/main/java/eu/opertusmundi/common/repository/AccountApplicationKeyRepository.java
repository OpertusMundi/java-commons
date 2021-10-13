package eu.opertusmundi.common.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountApplicationKeyEntity;
import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.account.AccountApplicationKeyDto;

@Repository
@Transactional(readOnly = true)
public interface AccountApplicationKeyRepository extends JpaRepository<AccountApplicationKeyEntity, Integer> {


    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.id = :id")
    Optional<AccountEntity> findOneAccountById(Integer id);

    @Query("SELECT k FROM AccountApplicationKey k WHERE k.account.id = :accountId and k.key = :key")
    Optional<AccountApplicationKeyEntity> findOneByAccountIdAndKey(Integer accountId, String key);

    @Query("SELECT k FROM AccountApplicationKey k WHERE k.account.key = :accountKey and k.key = :key")
    Optional<AccountApplicationKeyEntity> findOneByAccountKeyAndKey(UUID accountKey, String key);

    @Transactional(readOnly = false)
    default AccountApplicationKeyDto create(Integer accountId, String key) {
        final AccountApplicationKeyEntity entity  = new AccountApplicationKeyEntity(key);
        final AccountEntity               account = this.findOneAccountById(accountId).orElse(null);

        Assert.notNull(account, "Expected a non-null account");

        entity.setAccount(account);

        return this.saveAndFlush(entity).toDto();
    }

    @Transactional(readOnly = false)
    default AccountApplicationKeyDto revoke(Integer accountId, String key) {
        final AccountApplicationKeyEntity entity = this.findOneByAccountIdAndKey(accountId, key).orElse(null);

        Assert.notNull(entity, "Expected a non-null application key");

        entity.revoke();

        return this.saveAndFlush(entity).toDto();
    }

}
