package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.FileCopyResourceEntity;
import eu.opertusmundi.common.model.file.FileCopyResourceCommandDto;
import eu.opertusmundi.common.model.file.FileCopyResourceDto;

@Repository
@Transactional(readOnly = true)
public interface FileCopyResourceRepository extends JpaRepository<FileCopyResourceEntity, Integer> {

    @Query("SELECT f FROM FileCopyResource f WHERE f.idempotentKey = :idempotentKey")
    Optional<FileCopyResourceEntity> findOneByIdempotentKey(UUID idempotentKey);

    default Optional<FileCopyResourceDto> findOneObjectByIdempotentKey(UUID idempotentKey) {
        return this.findOneByIdempotentKey(idempotentKey).map(FileCopyResourceEntity::toDto);
    }

    @Query("SELECT f FROM FileCopyResource f WHERE f.accountKey = :userKey and f.assetPid = :assetPid")
    List<FileCopyResourceEntity> findAllByUserKeyAndAssetId(UUID userKey, String assetPid);

    @Query("SELECT f FROM FileCopyResource f WHERE f.accountKey = :userKey and f.completedOn is null")
    List<FileCopyResourceEntity> findAllActiveByUserKey(UUID userKey);

    default FileCopyResourceDto create(FileCopyResourceCommandDto command) {
        FileCopyResourceEntity e = new FileCopyResourceEntity();

        e.setAccountKey(command.getAccountKey());
        e.setAssetPid(command.getAssetPid());
        e.setResourceKey(command.getResourceKey());
        e.setSize(command.getSize());
        e.setSourcePath(command.getSourcePath());
        e.setTargetPath(command.getTargetPath());

        e = this.saveAndFlush(e);

        return e.toDto();
    }

    default FileCopyResourceDto fail(UUID idempotentKey, String errorMessage) {
        FileCopyResourceEntity e = this.findOneByIdempotentKey(idempotentKey).orElse(null);

        Assert.notNull(e, "Expected a non-null entity for idempotent key");
        Assert.isNull(e.getCompletedOn(), "Expected a null value for property completedOn");

        e.setCompletedOn(ZonedDateTime.now());
        e.setErrorMessage(errorMessage);
        e = this.saveAndFlush(e);

        return e.toDto();
    }

    default FileCopyResourceDto complete(UUID idempotentKey) {
        FileCopyResourceEntity e = this.findOneByIdempotentKey(idempotentKey).orElse(null);

        Assert.notNull(e, "Expected a non-null entity for idempotent key");
        Assert.isNull(e.getCompletedOn(), "Expected a null value for property completedOn");

        e.setCompletedOn(ZonedDateTime.now());
        e = this.saveAndFlush(e);

        return e.toDto();
    }

}
