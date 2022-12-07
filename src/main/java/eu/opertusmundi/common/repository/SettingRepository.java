package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.SettingEntity;
import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.SettingDto;
import eu.opertusmundi.common.model.SettingUpdateCommandDto;
import eu.opertusmundi.common.model.SettingUpdateDto;

@Repository
@Transactional(readOnly = true)
public interface SettingRepository extends JpaRepository<SettingEntity, Integer> {


    @Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
    Optional<HelpdeskAccountEntity> findAccountById(Integer id);

    @Query("SELECT s FROM Setting s ORDER BY s.service, s.key")
    List<SettingEntity> findAll();

    default List<SettingDto> findAllAsObjects() {
        return this.findAll().stream()
            .map(SettingEntity::toDto)
            .collect(Collectors.toList());
    }

    @Query("SELECT s FROM Setting s WHERE s.service = :service")
    List<SettingEntity> findAllByService(EnumService service);

    default List<SettingDto> findAllByServiceAsObjects(EnumService service) {
        return this.findAllByService(service).stream()
            .map(SettingEntity::toDto)
            .collect(Collectors.toList());
    }

    @Query("SELECT s FROM Setting s WHERE s.service = :service and s.key = :key")
    Optional<SettingEntity> findOneByServiceAndKey(EnumService service, String key);

    default SettingDto findOne(EnumSetting setting) {
        return this.findOneByServiceAndKey(setting.getService(), setting.getKey())
            .map(SettingEntity::toDto)
            .orElse(null);
    }

    @Transactional(readOnly = false)
    default void update(SettingUpdateCommandDto command) throws EntityNotFoundException {
        for (final SettingUpdateDto update : command.getUpdates()) {
            final SettingEntity         e = this.findOneByServiceAndKey(update.getService(), update.getKey()).orElse(null);
            final HelpdeskAccountEntity a = this.findAccountById(command.getUserId()).orElse(null);
            if (e == null) {
                throw new EntityNotFoundException(
                    String.format("Setting not found [service=%s, key=%s]",
                            update.getService(), update.getKey())
                );
            }

            e.setUpdatedBy(a);
            e.setUpdatedOn(ZonedDateTime.now());
            e.setValue(update.getValue());

            this.saveAndFlush(e);
        }
    }
}
