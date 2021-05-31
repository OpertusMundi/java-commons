package eu.opertusmundi.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.NotificationTemplateEntity;
import eu.opertusmundi.common.model.message.EnumNotificationType;

@Repository
@Transactional(readOnly = true)
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, Integer> {

    @Query("SELECT t FROM NotificationTemplate t WHERE t.type = :type")
    Optional<NotificationTemplateEntity> findOneByType(@Param("type") EnumNotificationType type);

}
