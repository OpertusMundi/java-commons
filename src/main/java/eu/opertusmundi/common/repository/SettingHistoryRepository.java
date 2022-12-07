package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.SettingHistoryEntity;
import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.SettingDto;
import eu.opertusmundi.common.model.SettingHistoryDto;

@Repository
@Transactional(readOnly = true)
public interface SettingHistoryRepository extends JpaRepository<SettingHistoryEntity, Integer> {

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.id = :id")
    Optional<HelpdeskAccountEntity> findAccountById(Integer id);

    @Query("SELECT h FROM SettingHistory h WHERE h.service = :service and h.key = :key ORDER BY h.updatedOn desc")
    List<SettingHistoryEntity> findAll(EnumService service, EnumSetting key);

    default List<SettingHistoryDto> findAllAsObjects(EnumSetting key) {
        return this.findAll(key.getService(), key).stream()
            .map(SettingHistoryEntity::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    default void create(SettingDto setting) throws EntityNotFoundException {
        var updatedBy = setting.getUpdatedBy() == null ? null : this.findAccountById(setting.getUpdatedBy().getId()).orElse(null);
        var history   = new SettingHistoryEntity();

        history.setKey(setting.getKey());
        history.setService(setting.getService());
        history.setType(setting.getType());
        history.setUpdatedBy(updatedBy);
        history.setUpdatedOn(setting.getUpdatedOn());
        history.setValue(setting.getValue());

        this.saveAndFlush(history);
    }
}
