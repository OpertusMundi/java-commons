package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;

@Repository
@Transactional(readOnly = true)
public interface ActivationTokenRepository extends JpaRepository<ActivationTokenEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<AccountEntity> findAccountById(@Param("id") Integer id);

    @Query("SELECT t FROM ActivationToken t WHERE t.account = :id and t.valid = true")
    List<ActivationTokenEntity> findAllByAccountId(Integer id);

    @Query("SELECT t FROM ActivationToken t WHERE t.token = :token and t.valid = true")
    Optional<ActivationTokenEntity> findOneByKey(UUID token);

    @Query("SELECT t FROM ActivationToken t INNER JOIN Account a on t.account = a.id "
         + "WHERE a.key = :key and t.valid = true and t.redeemedAt IS NULL and t.type = :type")
    Optional<ActivationTokenEntity> findActiveByTypeAndAccountKey(EnumActivationTokenType type, UUID key);

    @Modifying
    @Query("UPDATE ActivationToken t SET t.valid = false WHERE t.redeemedAt IS NULL and t.email = :email")
    void invalidateAllTokensForEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<AccountEntity> findAccountByEmail(@Param("email") String email);

    @Transactional(readOnly = false)
    default ActivationTokenDto create(Integer accountId, String email, int duration, EnumActivationTokenType type) {
        // Invalidate existing tokens
        this.invalidateAllTokensForEmail(email);

        // Create new token
        final ActivationTokenEntity token   = new ActivationTokenEntity();
        final AccountEntity         account = this.findAccountById(accountId).orElse(null);

        Assert.notNull(account, "Expected a non-null account");

        token.setAccount(account);
        token.setDuration(duration);
        token.setEmail(email);
        token.setType(type);
        token.setValid(true);

        this.saveAndFlush(token);

        return token.toDto();
    }

    @Transactional(readOnly = false)
    default void redeem(ActivationTokenEntity token) {
        if (token.getRedeemedAt() == null) {
            token.setRedeemedAt(ZonedDateTime.now());
        }

        this.saveAndFlush(token);
    }
}
