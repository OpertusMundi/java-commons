package eu.opertusmundi.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;

@Repository
@Transactional(readOnly = true)
public interface AssetFileTypeRepository extends JpaRepository<AssetFileTypeEntity, Integer> {

    @Query("SELECT t FROM AssetFileType t WHERE t.enabled = true")
    List<AssetFileTypeEntity> findAllEnabled();

    @Query("SELECT t FROM AssetFileType t WHERE t.format = :format")
    Optional<AssetFileTypeEntity> findOneByFormat(@Param("format") String format);

}