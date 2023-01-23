package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountCredentialsEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountCredentialsRepository extends JpaRepository<AccountCredentialsEntity, Integer> {

    @Query("""
        SELECT c FROM AccountCredentials c LEFT OUTER JOIN FETCH c.account a
        WHERE a.id = :id and c.application = :application
    """)
    Optional<AccountCredentialsEntity> findOneAccountByIdAndApplication(Integer id, String application);

}
