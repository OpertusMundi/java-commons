package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountRecentSearchEntity;
import eu.opertusmundi.common.model.account.AccountRecentSearchDto;

@Repository
@Transactional(readOnly = true)
public interface AccountRecentSearchRepository extends JpaRepository<AccountRecentSearchEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<AccountEntity> findOneAccountById(int id);

    @Query("SELECT r FROM AccountRecentSearch r WHERE r.account.id = :id")
    Page<AccountRecentSearchEntity> findAllByAccount(int id, Pageable pageable);

    default List<AccountRecentSearchDto> findAllObjectsByAccount(int id) {
        return this.findAllObjectsByAccount(id, 10);
    }

    default List<AccountRecentSearchDto> findAllObjectsByAccount(int id, int maxResults) {
        final PageRequest pageRequest = PageRequest.of(0, maxResults, Sort.by(Direction.DESC, "addedOn"));

        return this.findAllByAccount(id, pageRequest).map(AccountRecentSearchEntity::toDto).getContent();
    }

    @Transactional(readOnly = false)
    default void add(int userId, String value) {
        final AccountEntity account = this.findOneAccountById(userId).orElse(null);

        final AccountRecentSearchEntity e = new AccountRecentSearchEntity();
        e.setAccount(account);
        e.setValue(value);

        this.save(e);
    }

}
