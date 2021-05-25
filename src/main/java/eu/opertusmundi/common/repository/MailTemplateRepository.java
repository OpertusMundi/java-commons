package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.MailTemplateEntity;
import eu.opertusmundi.common.model.email.EnumMailType;

@Repository
@Transactional(readOnly = true)
public interface MailTemplateRepository extends JpaRepository<MailTemplateEntity, Integer> {

    @Query("SELECT t FROM MailTemplate t WHERE t.type = :type")
    Optional<MailTemplateEntity> findOneByType(@Param("type") EnumMailType type);

}
